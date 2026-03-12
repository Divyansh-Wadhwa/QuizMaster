document.addEventListener('DOMContentLoaded', () => {
    const container = document.querySelector('.iso-container');
    if (!container) return;

    // Clear existing content
    container.innerHTML = '';

    // Scene setup
    const scene = new THREE.Scene();

    // Camera setup
    const camera = new THREE.PerspectiveCamera(75, container.clientWidth / container.clientHeight, 0.1, 1000);
    camera.position.z = 20;
    camera.position.y = 0;

    // Renderer setup
    const renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true });
    renderer.setSize(container.clientWidth, container.clientHeight);
    renderer.setPixelRatio(window.devicePixelRatio);
    container.appendChild(renderer.domElement);

    // DNA Helix Group
    const dnaGroup = new THREE.Group();
    scene.add(dnaGroup);

    // Helix Parameters
    const tubeRadius = 0.4;
    const helixRadius = 5;
    const height = 18;
    const turns = 2.5;
    const pointsPerTurn = 40;
    const totalPoints = pointsPerTurn * turns;
    const yStep = height / totalPoints;

    // Particle Material (Glowing Dots)
    const particleMat = new THREE.PointsMaterial({
        size: 0.6,
        color: 0x00f2fe, // Cyan
        transparent: true,
        opacity: 0.8,
        blending: THREE.AdditiveBlending
    });

    const particleMat2 = new THREE.PointsMaterial({
        size: 0.6,
        color: 0xff00ff, // Magenta
        transparent: true,
        opacity: 0.8,
        blending: THREE.AdditiveBlending
    });

    // Connector Line Material
    const lineMat = new THREE.LineBasicMaterial({
        color: 0x4f8cff,
        transparent: true,
        opacity: 0.3
    });

    // Generate DNA Strands
    const strand1Geo = new THREE.BufferGeometry();
    const strand2Geo = new THREE.BufferGeometry();
    const pos1 = [];
    const pos2 = [];

    // Base pairs (horizontal lines connecting strands)
    const connectorsGroup = new THREE.Group();
    dnaGroup.add(connectorsGroup);

    for (let i = 0; i <= totalPoints; i++) {
        const angle = (i / pointsPerTurn) * Math.PI * 2;
        const y = (i * yStep) - (height / 2);

        // Strand 1 Position
        const x1 = Math.cos(angle) * helixRadius;
        const z1 = Math.sin(angle) * helixRadius;
        pos1.push(x1, y, z1);

        // Strand 2 Position (offset by PI)
        const x2 = Math.cos(angle + Math.PI) * helixRadius;
        const z2 = Math.sin(angle + Math.PI) * helixRadius;
        pos2.push(x2, y, z2);

        // Create connector line every few points (Base Pairs)
        if (i % 4 === 0) {
            const points = [];
            points.push(new THREE.Vector3(x1, y, z1));
            points.push(new THREE.Vector3(x2, y, z2));
            const connectorGeo = new THREE.BufferGeometry().setFromPoints(points);
            const connector = new THREE.Line(connectorGeo, lineMat);
            connectorsGroup.add(connector);
        }
    }

    strand1Geo.setAttribute('position', new THREE.Float32BufferAttribute(pos1, 3));
    strand2Geo.setAttribute('position', new THREE.Float32BufferAttribute(pos2, 3));

    const strand1 = new THREE.Points(strand1Geo, particleMat);
    const strand2 = new THREE.Points(strand2Geo, particleMat2);

    dnaGroup.add(strand1);
    dnaGroup.add(strand2);

    // Floating Background Particles (Data Motes)
    const bgParticlesGeo = new THREE.BufferGeometry();
    const bgParticleCount = 150;
    const bgPos = [];

    for (let i = 0; i < bgParticleCount; i++) {
        bgPos.push((Math.random() - 0.5) * 40); // x
        bgPos.push((Math.random() - 0.5) * 40); // y
        bgPos.push((Math.random() - 0.5) * 40); // z
    }

    bgParticlesGeo.setAttribute('position', new THREE.Float32BufferAttribute(bgPos, 3));
    const bgParticlesMat = new THREE.PointsMaterial({
        size: 0.15,
        color: 0xffffff,
        transparent: true,
        opacity: 0.3
    });
    const bgParticles = new THREE.Points(bgParticlesGeo, bgParticlesMat);
    scene.add(bgParticles);


    // Mouse Interaction
    let mouseX = 0;
    let mouseY = 0;
    let targetRotationX = 0;
    let targetRotationY = 0;

    // Add event listener to document instead of window for better interaction
    document.addEventListener('mousemove', (event) => {
        mouseX = (event.clientX - window.innerWidth / 2) * 0.001;
        mouseY = (event.clientY - window.innerHeight / 2) * 0.001;
    });

    // Animation Loop
    let time = 0;
    function animate() {
        requestAnimationFrame(animate);
        time += 0.01;

        // Auto Rotation
        dnaGroup.rotation.y += 0.01;

        // Gentle Float
        dnaGroup.position.y = Math.sin(time) * 0.5;

        // Mouse Parallax (smooth interpolation)
        targetRotationY = mouseX * 0.5;
        targetRotationX = mouseY * 0.5;

        dnaGroup.rotation.x += 0.05 * (targetRotationX - dnaGroup.rotation.x);
        // We add to the auto-rotation (y) so we don't overwrite it, just tilt it
        dnaGroup.rotation.z += 0.05 * (-targetRotationY - dnaGroup.rotation.z);

        // Pulse Effect (Scale)
        const scale = 1 + Math.sin(time * 2) * 0.02;
        dnaGroup.scale.set(scale, scale, scale);

        // Rotate background particles differently
        bgParticles.rotation.y -= 0.002;
        bgParticles.rotation.x += 0.001;

        renderer.render(scene, camera);
    }

    animate();

    // Handle Resize
    window.addEventListener('resize', () => {
        const width = container.clientWidth;
        const height = container.clientHeight;

        renderer.setSize(width, height);
        camera.aspect = width / height;
        camera.updateProjectionMatrix();
    });
});
