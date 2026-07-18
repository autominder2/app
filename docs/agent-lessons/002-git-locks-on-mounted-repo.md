Lesson: Stale .git/*.lock files accumulate on this mounted repo because the sandbox cannot unlink them during commits; clear them before every commit.
Detail: Fix = enable file deletion for the mount (allow_cowork_file_delete), then `find .git -maxdepth 2 -name "*.lock" -delete` before `git commit`. Symptom otherwise: "cannot lock ref 'HEAD'". Confirmed repeatedly 2026-07-17/18.
