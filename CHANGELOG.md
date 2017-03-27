# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).
## [0.26.6] - 2017-03-27
### Fixed
- EntityExists expection for duplicate identities on save (InMemoryIdentityRepository)

## [0.26.5] - 2017-03-24
### Fixed
- EntityExists expection for duplicate identities on save

## [0.26.4] - 2017-01-30
### Changed
- (BC break) moved drop related classes from http package into drop package
- (BC break) moved drop http related classes from http package into drop/http
- dependencies updated #664

### Added
- client module #667
- local storage for files and DMs #669
- box interactors from android moved to client module

## [0.26.3] - 2016-11-02
### Added
- persistent Account.token Qabel/qabel-desktop#474

## [0.26.2] - 2016-10-30
### Changed
- (BC break) EventSource.events is now a property and events() infers type
- (BC break) moved all drop related classes from http package into drop package

## [0.26.0] - 2016-10-24
### Added
- metaVolumes (for cross client sync) #496
- license report

### Changed
- moved VolumeFactories from desktop client to core #631
- ensure UTF-8 on drop message decryption Qabel/qabel-desktop#518
- emoji filter for avatars #635
- extracted RequestAuthorizer from BoxClient #614

## [0.25.7]
