# GitHub Pages demo

The demo is prepared for `https://kokutbon.github.io/ExAMEN/`. It has not been published by this setup. The deployment workflow runs only when you manually select **Run workflow**; it has no push or pull-request trigger.

## Build and preview

Use Node.js 22.12+ (or a supported newer LTS release).

```bash
cd examen-frontend
npm ci
npm run build:demo
npm run preview:demo
```

Open `http://127.0.0.1:4300/ExAMEN/`. The static output is `examen-frontend/dist/examen-demo/browser/`. Serve or upload that directory, not the parent directory. There is no server bundle or Node.js runtime requirement for the hosted demo.

The preview intentionally serves the project path with normal static file behavior. Navigation uses URLs such as `/ExAMEN/#/journal`, so reloading or sharing a feature link works without server rewrite rules or a custom 404 redirect.

## Demo behavior

- Visitors start with a synthetic demo identity and can select **Explore demo** to enter the app. No registration or password is needed.
- Categories, questions, todos, journal entries, sessions, gratitude, habits, settings, and feedback operate in the browser. Changes persist across refreshes in namespaced `examen-demo:` local storage.
- **Reset demo** removes only the demo's storage and reloads its sample data. Other apps' browser storage is preserved.
- Profile statistics reflect the local demo records. Insight text and suggestions are deterministic examples, with no Gemini calls.
- JSON data export works locally. PDF export, email delivery, password recovery, and scheduled reminders require the self-hosted app and are unavailable in the demo. Reminder controls and PDF export are disabled.
- Authentication forms simulate access to the same demo identity; passwords are never stored or transmitted. Account deletion resets only demo data.
- The demo is for exploration with sample text. Browser-local data is not a private server account or a backup, and is cleared when site storage is cleared.

`src/main.demo.ts` provides a separate client-only bootstrap with hash routing and an HTTP interceptor that never forwards requests. The `demo` build replaces the environment configuration and disables SSR, prerendering, and hydration. The standard `npm run build` retains the existing SSR deployment.

## Validation

```bash
npm run typecheck
npm test -- --watch=false
npm run build:demo
npm run e2e:demo
npm run build
```

Install Chromium once with `npm run e2e:install` if needed. The demo browser tests use the actual static build without API mocks: they exercise navigation, deep-link refresh, persisted journal edits, JSON export, storage reset, and an examination submission, and check for unintended API requests. CI builds and tests the demo and saves an artifact without publishing it.

## Publish when ready

The repository can remain private. For a repository owned by a personal account, GitHub Pro supports Pages from private repositories; GitHub Team provides that support for organization-owned repositories. The published demo is publicly accessible even when its source repository is private. See [GitHub's plans](https://docs.github.com/en/get-started/learning-about-github/githubs-plans).

This configuration publishes a project site at `https://kokutbon.github.io/ExAMEN/`. To publish at `https://kokutbon.github.io/` instead, use a repository named `kokutbon.github.io` and change the demo base path, preview server, browser tests, and social-preview URL to the root path. See [GitHub Pages site types](https://docs.github.com/en/pages/getting-started-with-github-pages/what-is-github-pages).

1. Commit and push the prepared changes to `KOKUTBON/ExAMEN` when ready.
2. In the repository, open **Settings → Pages → Build and deployment** and select **GitHub Actions** as the source.
3. Open **Actions → Publish GitHub Pages demo (manual)**.
4. Select **Run workflow** on the branch containing these changes.
5. The workflow builds and tests the static demo, uploads its Pages artifact, and deploys it. The deployment job reports the published URL.

Subsequent pushes do not redeploy. Repeat the manual workflow to publish an update. No API secrets, database credentials, or backend hosting are required.

The `/ExAMEN/` path is case-sensitive. For another repository name, change `baseHref` in the `demo` configuration of `angular.json`, the preview server and tests, and the social-preview URL in `scripts/prepare-demo.mjs`.

References: [Angular build configuration](https://angular.dev/cli/build) and [GitHub Pages custom workflows](https://docs.github.com/en/pages/getting-started-with-github-pages/using-custom-workflows-with-github-pages).
