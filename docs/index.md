---
title: PageKeeper Privacy Policy
---

# Privacy Policy for PageKeeper

_Last updated: 2026-04-15_

PageKeeper ("the app") is an Android application for managing a personal
library of FB2 ebooks. This policy explains what information the app
handles and how.

## Information the app collects

PageKeeper **does not collect, transmit, or share any personal data**.

- No user accounts or sign-in.
- No analytics, tracking, or advertising SDKs.
- No crash-reporting services.

## Data stored on your device

All book files, cover images, and library metadata are stored locally on
your device in the app's private storage:

- FB2 files you import (`<app-private-storage>/*.fb2`)
- Cover images extracted from those files (`<app-private-storage>/*.png`)
- Parsed page content for the reading view (`<app-private-storage>/*.json`)
- A local database containing book titles, authors, and your
  favorite/finished flags

This data never leaves your device unless you explicitly share a book
through Android's share sheet (e.g. to Gmail or Drive). In that case, a
copy of the selected file is placed in the app's cache directory and
handed to the receiving app via Android's FileProvider.

## Permissions

- **Storage access** — used only when you pick an FB2 file to import.
  The app reads the file you selected and copies it into its own private
  storage.

## Children's privacy

The app does not knowingly collect any information from anyone,
including children under 13.

## Changes to this policy

If this policy changes, the updated version will be published at the
same URL with a new "Last updated" date.

## Contact

Questions about this policy can be sent to:
**...**