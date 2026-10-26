# Lensometer V7 — Research notes and next experimental checkpoint
Date: 2026-10-25
Classification: research proposal, not experimentally validated

## Baseline and migration
- Source repository: https://github.com/moeotech/lensometer-v2 (main at migration time).
- Current working repository: https://github.com/alnatourm/LENSOMETERV7
- Read the root `Lensometer_V2_RnD_Tracker.docx` before planning new changes.
- V4/V5/V6 are historical experimental implementations. Do not overwrite functioning optical modules until a test demonstrates the benefit of a proposed replacement.
- Known test lens: SPH -0.75 D, CYL -1.00 D. The axis is **not known**; do not use this prescription to adjust outputs or fit individual measurements.

## External image observations, supplied on 2026-10-25
Two screenshots show (a) a spectacle lens imaged against a pale background with markers; and (b) six lens-contour overlays comparing a target outline, an original detected outline, and a scaled outline. The displayed target width is 53.7 mm; the green overlay is labelled approximately 1.13x. The panels vary in angle and pixels-per-mm. These images support investigation of **physical lens outline localization and dimensional registration**, but do not establish that the demonstrator can measure SPH, CYL or AXIS. Unknown: source, software, calibration, measurement accuracy.

## Our proposed scientific approach
1. Segment the **actual lens boundary** as a polygon/mask and estimate its uncertainty. A circular fallback ROI may omit relevant dot cells or include background.
2. Preserve V5 dense dot correspondence when independently validated. In a prior screenshot, V5 reported 741 matches, 97.6% coverage and registration RMS ~1.58 px. High matching coverage is not evidence of accurate lens power.
3. Estimate global camera/target registration using reference points **outside** the physical lens; do not normalize away lens-induced scale inside the lens, which can erase spherical optical signal.
4. Fit a robust 2D displacement field from registered no-lens versus lens captures. Derive Jacobian and its directional symmetric structure. The Jacobian is an optical deformation observable, **not a diopter reading**.
5. Calibrate with a measured camera-lens-target forward model, fixed distances, known spherical and cylindrical lenses, independent holdout validation, and repeatability error bars.

## V6 ROI failure: hypothesis, not resolved diagnosis
Historical screenshot: FALLBACK ROI center (320,240), inner radius 120, outer radius 192, zero accepted vectors and zero cells inside ROI. This does **not** by itself prove coordinate mismatch. Consider: wrong center/segmentation, central point-detection void, index mapping mismatch, incompatible coordinate spaces, or incorrect image rotation/crop. Add telemetry for canonical pixel coordinate frame, bitmap dimensions, point bounds, mask overlay, ROI occupancy and nearest-cell distances.

## Proposed decisive experiment (not yet performed)
1. Clamp phone and target; lock focus/exposure as far as supported. Record camera-to-lens and lens-to-target distances.
2. Take no-lens reference without moving camera/target.
3. Insert known cylindrical lens; capture five repeats.
4. Rotate the lens in plane by 90 degrees, keeping center/tilt fixed; capture five further repeats.
5. Compare anisotropy magnitude, principal orientation (periodic with 180 degrees), repeatability and tracking confidence. Lens-locked anisotropy should co-rotate by about 90 degrees, accounting for coordinate conventions.
6. Include a plano control and an out-of-lens registration control. Avoid interpreting noisy nearly-isotropic orientation estimates as meaningful.

## Development gates
- **Gate A:** V6 ROI audit: visually prove physical mask and canonical cell coordinates agree.
- **Gate B:** V7 dense field: obtain repeatable physically plausible displacement and uncertainty maps.
- **Gate C:** Rotation: demonstrate direction rotates with the real cylinder lens.
- **Gate D:** Calibrate positive/negative spheres, cylinders and known axes.
- **Gate E:** Verify on held-out lenses; only then expose experimental SPH/CYL/AXIS (never clinical claims without independent validation).

## Documentation rule
For every physical experiment, record: date, git commit SHA, phone/camera settings, target version, lens type/axis, three physical distances, reference and lens images, raw logs, registration, ROI mask, point counts, residuals, anisotropy, interpretation, and the single next action.

**Current status:** Source migrated; scientific V7 proposed but unimplemented. No live physical validation or successful calibration established.
