package com.rei.examenbackend.service;

import com.rei.examenbackend.model.ExaminationSession;
import com.rei.examenbackend.model.GratitudeEntry;
import com.rei.examenbackend.model.ToDoItem;
import com.rei.examenbackend.model.User;
import com.rei.examenbackend.repository.ExaminationSessionRepository;
import com.rei.examenbackend.repository.GratitudeEntryRepository;
import com.rei.examenbackend.repository.ToDoItemRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class GrowthReportService {
    private static final PDFont FONT_BODY = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final float FONT_BODY_SIZE = 11f;
    private static final float FONT_SECTION_SIZE = 14f;
    private static final float FONT_TITLE_SIZE = 20f;
    private static final float MARGIN = 48f;
    private static final float LEADING = 16f;

    private final ExaminationSessionRepository sessionRepository;
    private final GratitudeEntryRepository gratitudeRepository;
    private final ToDoItemRepository todoRepository;

    public GrowthReportService(ExaminationSessionRepository sessionRepository,
                               GratitudeEntryRepository gratitudeRepository,
                               ToDoItemRepository todoRepository) {
        this.sessionRepository = sessionRepository;
        this.gratitudeRepository = gratitudeRepository;
        this.todoRepository = todoRepository;
    }

    public byte[] buildReport(User user) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(29);
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = today.atTime(23, 59, 59);

        List<ExaminationSession> sessions = sessionRepository
                .findAllByUserAndCompletedAtBetweenOrderByCompletedAtDesc(user, startDateTime, endDateTime)
                .stream()
                .filter(session -> session.getCompletedAt() != null)
                .sorted(Comparator.comparing(ExaminationSession::getCompletedAt))
                .toList();

        List<GratitudeEntry> gratitude = gratitudeRepository
                .findByUserAndCreatedAtBetweenOrderByCreatedAtDesc(user, startDateTime, endDateTime);

        List<ToDoItem> todos = todoRepository.findByUserOrderByDueAtAsc(user);

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ReportWriter writer = new ReportWriter(document);
            writer.begin();

            DateTimeFormatter headerFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);
            String period = headerFormatter.format(startDate) + " - " + headerFormatter.format(today);

            writer.writeLine("Examen Growth Summary", FONT_BOLD, FONT_TITLE_SIZE);
            writer.writeLine("Prepared for " + user.getFullName(), FONT_BODY, FONT_BODY_SIZE);
            writer.writeLine("Period: " + period, FONT_BODY, FONT_BODY_SIZE);
            writer.addSpacer(6f);

            writer.writeSection("Mood trend");
            writer.writeParagraph(buildMoodSummary(sessions), FONT_BODY, FONT_BODY_SIZE);
            if (sessions.isEmpty()) {
                writer.writeParagraph("No completed examinations in this period.", FONT_BODY, FONT_BODY_SIZE);
            } else {
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.US);
                for (ExaminationSession session : sessions) {
                    String date = dateFormatter.format(session.getCompletedAt());
                    String mood = session.getMoodScore() == null ? "-" : session.getMoodScore() + "/5";
                    String notes = session.getNotes() == null ? "" : session.getNotes().trim();
                    String line = notes.isEmpty()
                            ? String.format("%s  -  Feeling %s", date, mood)
                            : String.format("%s  -  Feeling %s  -  %s", date, mood, notes);
                    writer.writeBullet(line);
                }
            }
            writer.addSpacer(10f);

            writer.writeSection("Gratitude highlights");
            if (gratitude.isEmpty()) {
                writer.writeParagraph("No gratitude entries logged this period.", FONT_BODY, FONT_BODY_SIZE);
            } else {
                DateTimeFormatter gratitudeFormatter = DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.US);
                for (GratitudeEntry entry : gratitude.stream().limit(10).toList()) {
                    String line = gratitudeFormatter.format(entry.getCreatedAt()) + "  -  " + entry.getContent();
                    writer.writeBullet(line);
                }
            }
            writer.addSpacer(10f);

            writer.writeSection("Todos snapshot");
            long openCount = todos.stream().filter(todo -> !todo.isCompleted()).count();
            long completedCount = todos.stream().filter(ToDoItem::isCompleted).count();
            writer.writeParagraph("Open todos: " + openCount + "  -  Completed: " + completedCount, FONT_BODY, FONT_BODY_SIZE);

            List<ToDoItem> openTodos = todos.stream()
                    .filter(todo -> !todo.isCompleted())
                    .limit(8)
                    .toList();
            if (!openTodos.isEmpty()) {
                writer.writeParagraph("Upcoming:", FONT_BODY, FONT_BODY_SIZE);
                for (ToDoItem todo : openTodos) {
                    writer.writeBullet(formatTodo(todo));
                }
            } else {
                writer.writeParagraph("No open todos right now.", FONT_BODY, FONT_BODY_SIZE);
            }

            List<ToDoItem> completedTodos = todos.stream()
                    .filter(todo -> todo.isCompleted() && todo.getUpdatedAt() != null
                            && !todo.getUpdatedAt().isBefore(startDateTime))
                    .limit(8)
                    .toList();
            if (!completedTodos.isEmpty()) {
                writer.addSpacer(6f);
                writer.writeParagraph("Recently completed:", FONT_BODY, FONT_BODY_SIZE);
                for (ToDoItem todo : completedTodos) {
                    writer.writeBullet(formatTodo(todo));
                }
            }

            writer.finish();
            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to generate PDF report", e);
        }
    }

    private String buildMoodSummary(List<ExaminationSession> sessions) {
        if (sessions.isEmpty()) {
            return "This section summarizes your recorded feelings over the last 30 days.";
        }
        double average = sessions.stream()
                .map(ExaminationSession::getMoodScore)
                .filter(score -> score != null)
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
        String avgText = String.format(Locale.US, "%.1f", average);
        return "Average feeling score: " + avgText + "/5 across " + sessions.size() + " sessions.";
    }

    private String formatTodo(ToDoItem todo) {
        String due = todo.getDueAt() == null
                ? "No due date"
                : todo.getDueAt().toLocalDate().toString();
        String status = todo.isCompleted() ? "Completed" : "Open";
        return todo.getTitle() + "  -  " + status + "  -  " + due;
    }

    private static final class ReportWriter {
        private final PDDocument document;
        private PDPage page;
        private PDPageContentStream contentStream;
        private float cursorY;
        private float usableWidth;

        private ReportWriter(PDDocument document) {
            this.document = document;
        }

        void begin() throws IOException {
            newPage();
        }

        void finish() throws IOException {
            if (contentStream != null) {
                contentStream.close();
            }
        }

        void writeSection(String title) throws IOException {
            addSpacer(4f);
            writeLine(title, FONT_BOLD, FONT_SECTION_SIZE);
            addSpacer(2f);
        }

        void writeLine(String text, PDFont font, float fontSize) throws IOException {
            text = sanitize(text, font);
            ensureSpace(LEADING);
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(MARGIN, cursorY);
            contentStream.showText(text);
            contentStream.endText();
            cursorY -= LEADING;
        }

        void writeParagraph(String text, PDFont font, float fontSize) throws IOException {
            List<String> lines = wrapText(sanitize(text, font), font, fontSize, usableWidth);
            for (String line : lines) {
                writeLine(line, font, fontSize);
            }
        }

        void writeBullet(String text) throws IOException {
            String bullet = sanitize("- " + text, FONT_BODY);
            List<String> lines = wrapText(bullet, FONT_BODY, FONT_BODY_SIZE, usableWidth);
            for (String line : lines) {
                writeLine(line, FONT_BODY, FONT_BODY_SIZE);
            }
        }

        void addSpacer(float space) {
            cursorY -= space;
        }

        private void ensureSpace(float needed) throws IOException {
            if (cursorY - needed < MARGIN) {
                newPage();
            }
        }

        private void newPage() throws IOException {
            if (contentStream != null) {
                contentStream.close();
            }
            page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            cursorY = page.getMediaBox().getHeight() - MARGIN;
            usableWidth = page.getMediaBox().getWidth() - (2 * MARGIN);
        }

        /**
         * The Standard 14 fonts only cover WinAnsi characters; replace anything else (emoji, most
         * non-Latin scripts) and control characters so a single entry cannot break the whole export.
         */
        private static String sanitize(String text, PDFont font) {
            if (text == null) {
                return "";
            }
            StringBuilder out = new StringBuilder(text.length());
            text.codePoints().forEach(cp -> {
                if (Character.isWhitespace(cp) || Character.isISOControl(cp)) {
                    out.append(' ');
                    return;
                }
                String ch = new String(Character.toChars(cp));
                try {
                    font.encode(ch);
                    out.append(ch);
                } catch (IOException | IllegalArgumentException ex) {
                    out.append('?');
                }
            });
            return out.toString();
        }

        private List<String> wrapText(String text, PDFont font, float fontSize, float width) throws IOException {
            List<String> lines = new ArrayList<>();
            String[] words = text.split("\\s+");
            StringBuilder current = new StringBuilder();
            for (String word : words) {
                String candidate = current.length() == 0 ? word : current + " " + word;
                float candidateWidth = font.getStringWidth(candidate) / 1000 * fontSize;
                if (candidateWidth > width && current.length() > 0) {
                    lines.add(current.toString());
                    current = new StringBuilder(word);
                } else {
                    current = new StringBuilder(candidate);
                }
            }
            if (current.length() > 0) {
                lines.add(current.toString());
            }
            return lines;
        }
    }
}
