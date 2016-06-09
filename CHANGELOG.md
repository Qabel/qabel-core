## [Unreleased]
### CHANGES
- AbstractNavigation::upload no longer throws QblStorageNameConflictExceptions (they are just resolved)
- #488 FileNameConflicts are resolved in reverse order. The remote file is renamed now.
