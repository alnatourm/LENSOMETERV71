# V7 optical measurement engineering notes (2026-10-31)

## Research
- Guan et al., Optical Engineering 61(12), 121809 (2022), DOI 10.1117/1.OE.61.12.121809: smartphone camera, **real displayed/printed target**, fixed target distance, dimensional reference, lens magnification field, SPH/CYL/AXIS.
- Lee et al., Applied Optics 53, H1-H6 (2014), DOI 10.1364/AO.53.0000H1: camera distortion and keystone calibration.
- Clinical lensometry requires vertex-power and axis calibration; frame-edge center is not necessarily optical center.

## Physics and identifiability
- The app's white Hermann overlay is drawn **after** the light is captured. It cannot be refracted by the spectacle lens.
- The lens ellipse provides an aperture mask and a geometric center, NOT optical power or optical center.
- Unknown camera-to-lens distance, target depth, tilt, and camera intrinsics confound conversion of image magnification to diopters.
- For a thin lens with object-to-lens distance u, lens-to-camera distance v, and local power F (m^-1), a paraxial ray model can be used to derive the image scale, but it is invalid to directly equate measured pixel displacement with diopters. Validate sign conventions, target geometry and vertex correction experimentally.
- An affine displacement model D(p)=B(p-c)+t yields local Jacobian J=I+B. Principal stretches are singular values of J; principal axes describe image stretch, NOT automatically the clinical cylinder axis.
- Optical center needs a stable displacement-field estimate and independent prism/decentration checks. Do not assume rim center is optical center.

## V7 current behavior
- Capture baseline scene without lens; detect natural image corners; detect ellipse on lens-present frame.
- Partition tracked features into interior and exterior using ellipse-normalized radius.
- Use exterior median translation to subtract gross motion; fit interior affine distortion; report pixel displacement, principal image magnifications and residual.
- Do not report SPH/CYL/AXIS in diopters before calibrated validation.

## Required next engineering gates
1. Android compile and instrumentation verification on exact main commit.
2. Lens-rim temporal tracking with confidence, hysteresis, ellipse aspect/area consistency; reject false circles.
3. Optical-flow forward/backward validation and RANSAC exterior homography/affine camera motion compensation (translation alone is insufficient).
4. Reject blurred frames, focus changes, auto-zoom, poor illumination, insufficient feature coverage, lens tilt, high fit residual, and high motion.
5. Calibrate camera intrinsics, distortion, lens-to-camera spacing and target depth. Use a real scene target with known geometry or a reference optical fixture for quantitative power.
6. Collect known single-vision lenses across positive/negative sphere, cylinder and axis, repeated orientations and distances. Fit and cross-validate a physical model, not a direct linear guess from singular values.
7. Evaluate against a calibrated commercial lensmeter using MAE, 95% limits of agreement, repeatability and failure rate. Separate sphere, cylinder, axis and prism. Never use results clinically without validation.

## Minimal UI
- Full-screen fixed Hermann grid overlay for alignment only; cyan stabilized rim, yellow center.
- SET REFERENCE; RESET; inside X, Y, RMS and fit quality; confidence status.
- No prescription display until calibration gates pass.
