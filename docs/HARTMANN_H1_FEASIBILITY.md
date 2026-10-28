# Lensometer V7: Hartmann H1 Feasibility Prototype

Status: engineering research / NOT calibrated / NOT for dispensing.

## Objective
Test whether an ordinary Android phone plus a low-cost controlled optical module can resolve Hartmann beam deflections sufficiently to estimate the sphere, cylinder and axis of a loose spectacle lens held by hand.

## Research basis
- https://pmc.ncbi.nlm.nih.gov/articles/PMC5368642/ (microlens array, CMOS sensor at focal plane, reference registration)
- https://www.activeopticalsystems.com/wp-content/uploads/Hartmann-Sensor-Manual.pdf (aperture-array Hartmann sensor with webcam)
- https://opg.optica.org/ao/abstract.cfm?uri=ao-47-34-6434 (ophthalmic lens wavefront measurement)
- https://www.ncbi.nlm.nih.gov/sites/books/NBK597365/ (lens stop and stabilization requirements)

## Proposed optical path
Diffused LED -> collimating lens -> hand-held test lens at guided measurement plane -> precision aperture mask -> imaging optics -> smartphone sensor.

IMPORTANT: Smartphone camera is not a bare sensor at the mask focal plane. The coupling optics, aperture dimensions and focus must be designed and experimentally verified. A paper dot grid is NOT a Hartmann mask.

## H1 hardware
1. Existing Android phone, locked exposure, ISO, focus, zoom and resolution.
2. Low-power visible LED with diffuser; avoid lasers and direct viewing of intense sources.
3. Collimating optical assembly, adjustable during bench alignment.
4. Opaque precision aperture array. Evaluate pitches and aperture diameters using optical ray tracing and camera resolution; do not prescribe final dimensions before camera characterization.
5. Rigid phone/mask/collimator fixture, preferably 3D printed.
6. Hand-held lens guide defining the axial measurement plane. No contact with optical surfaces required.
7. No-lens baseline calibration plus independently verified spherical and cylindrical test lenses.

## Feasibility gates
G0: Record phone camera intrinsics, focus behavior, pixel pitch if available, image pipeline and raw capture support.
G1: Without test lens, capture >=100 distinguishable, unsaturated stable spots, with repeatable centroids and known indexing. This is an experimental acceptance target, not a guarantee.
G2: With test lens, track spot identities and displacements; reject saturated, missing, merged and ambiguously indexed spots.
G3: Estimate and remove global pointing/tilt terms without erasing the lens curvature signal. Quantify residuals.
G4: Recover a quadratic wavefront fit, principal curvatures and axis. Map to diopters only using validated optical geometry and certified reference lenses.
G5: Five repeated hand-held captures per reference lens, several lens positions and rotations. Compare with calibrated commercial lensmeter; report errors and uncertainty.
G6: Show SPH/CYL/AXIS only if independently calibrated, stable and within predeclared accuracy tolerances; otherwise display RESEARCH / N/A.

## Software architecture
- HartmannCapture: controlled camera acquisition and exposure stability.
- SpotDetector: subpixel centroid detection, quality and saturation filters.
- SpotMatcher: match reference and lens spots with uniqueness checks.
- WavefrontFitter: local slopes and robust 2D polynomial fit.
- LensPowerEstimator: calibrated SPH/CYL/AXIS and sign/axis conventions.
- MeasurementQuality: repeatability, geometry, movement and uncertainty rejection.
- Telemetry export: image sizes, calibration ID, geometry, centroids, rejected spots, residuals and quality reasons.
Keep existing V4/V5/V6 printed-target analyzer intact for A/B comparison.

## First experiment
Take a baseline capture without lens, then captures with a known spherical lens, a known sphero-cylinder lens, and 90-degree rotation. Evaluate whether centroid deflections are reproducible. Do not assert prescription accuracy from a successful spot fit alone.

## Key risks
- Smartphone camera optics and autofocus confound angular deflection.
- Mask spot overlap and limited dynamic range.
- Lens tilt, decentration, motion and variable lens-to-mask distance.
- Camera image processing warps geometry.
- A handheld lens may require a physical guide to meet accuracy targets.
