# Demo data for screenshots

`Calc.java` — `computeUnsafe` flagged; `computeSafe` not flagged.

## How to get the screenshot

1. `./gradlew runIde` from `integer-overflow-widening-companion`, open
   this `demo/` folder as the project.
2. Full Screen, open `Calc.java` — a warning should appear on
   `computeUnsafe`'s `a * b` line but not on `computeSafe`'s.
3. Screenshot with both methods visible, save into
   `integer-overflow-widening-companion/docs/screenshots/`. Close the
   sandbox.
