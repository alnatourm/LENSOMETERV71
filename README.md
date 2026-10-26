# Lensometer V7

This is the active GitHub repository for the smartphone optical lensometer R&D project.

## Source migration verification (2026-10-25)

Source: `moeotech/lensometer-v2`, default branch `main`.

**Verified:** All 161 source-repository files have been copied to this repository with their paths and Git blob SHA-1 identifiers matching the source tree. This includes Android Kotlin source, build files, V4/V5/V6 analysis, test scripts, icon assets and `Lensometer_V2_RnD_Tracker.docx`.

The original repository was **not modified**. This file migration does not copy the source commit history, branches, GitHub Actions settings, repository secrets or issue/PR metadata. No Android build or device test has been performed in the new repository yet.

## Documentation

- [Living R&D tracker](Lensometer_V2_RnD_Tracker.docx)
- [V5 architecture](docs/V5_DEFLECTOMETRY_ARCHITECTURE.md)
- [V6 architecture](docs/V6_STRUCTURED_DEFLECTOMETRY.md)
- [External contour study and V7 proposal](docs/2026-10-25_EXTERNAL_CONTOUR_AND_V7_RESEARCH.md)

## Next scientific checkpoint

1. Read the R&D tracker and research proposal.
2. Validate V6 lens ROI, point coordinates and segmentation on real frames.
3. Keep the V5 matcher as a baseline while exploring a dense 2D optical displacement field.
4. Test cylinder-axis rotation with rigid camera/target geometry, repeat captures and uncertainty reporting.
5. Calibrate only after independent repeatability and holdout validation.

**Reference images status:** The two new external demonstration screenshots shared in the conversation on 2026-10-25 are not yet committed as image files. They are described in the research notes.

**Measurement status:** This is research software; no clinical precision or calibrated SPH/CYL/AXIS readings are established.
