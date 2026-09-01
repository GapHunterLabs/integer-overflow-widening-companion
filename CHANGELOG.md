<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Integer-Overflow-on-Widening Companion Changelog

## [Unreleased]

## [0.1.0]

### Added

- Warning on an `int * int`/`int + int` expression assigned/passed
  directly to a `long`-typed destination with no widening cast on
  either operand -- CWE-190, Integer Overflow or Wraparound.
- Checks every real destination shape: variable initializer,
  assignment, method argument, and method return.

[Unreleased]: https://github.com/GapHunterLabs/integer-overflow-widening-companion/compare/0.1.0...HEAD
[0.1.0]: https://github.com/GapHunterLabs/integer-overflow-widening-companion/commits/0.1.0
