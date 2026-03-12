/* Decorative Three.js 3D accent (non-intrusive)
   - Creates a small, interactive rotating 3D emblem in the bottom-right corner
   - Canvas uses pointer-events: none so it never interferes with page functionality
   - Depends on global THREE (include CDN before this script)
*/
(function () {
    try {
        if (typeof THREE === 'undefined') return;
        const size = 220; // logical size of the decorative canvas

        // Container
        const container = document.createElement('div');
        container.id = 'three-canvas-container';
        container.style.position = 'fixed';
        container.style.right = '28px';
        container.style.bottom = '28px';
        container.style.width = size + 'px';
        container.style.height = size + 'px';
        container.style.zIndex = 9999;
        container.style.pointerEvents = 'none';
        container.style.opacity = '0.98';
        container.style.transition = 'transform 240ms cubic-bezier(.2,.9,.2,1), opacity 220ms';
        document.body.appendChild(container);

        const canvas = document.createElement('canvas');
        canvas.id = 'three-canvas';
        canvas.style.width = '100%';
        canvas.style.height = '100%';
        canvas.style.display = 'block';
        canvas.style.pointerEvents = 'none';
        container.appendChild(canvas);

        const renderer = new THREE.WebGLRenderer({ canvas: canvas, alpha: true, antialias: true });
        renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
        renderer.setClearColor(0x000000, 0);

        const scene = new THREE.Scene();

        const camera = new THREE.PerspectiveCamera(35, 1, 0.1, 1000);
        camera.position.set(0, 0, 60);

        // Lights
        const hemi = new THREE.HemisphereLight(0xffffff, 0x444444, 0.8);
        scene.add(hemi);
        const dir = new THREE.DirectionalLight(0xffffff, 1.0);
        dir.position.set(20, 20, 40);
        scene.add(dir);

        // Low-poly shiny emblem (ico + subtle bevel)
        const geom = new THREE.IcosahedronGeometry(12, 1);
        // create a detail by modifying vertices a bit
        try {
            geom.attributes.position.array.forEach((v, idx) => {
                // small random noise on positions for a handcrafted low-poly feel
                geom.attributes.position.array[idx] = v + (Math.random() - 0.5) * 0.3;
            });
            geom.attributes.position.needsUpdate = true;
            geom.computeVertexNormals();
        } catch (err) {
            // older/newer Three builds may not support this exact approach — ignore safely
        }

        const material = new THREE.MeshStandardMaterial({
            color: 0x6ee7ff,
            metalness: 0.55,
            roughness: 0.15,
            emissive: 0x072534,
            emissiveIntensity: 0.06,
            envMapIntensity: 1.0
        });
        const mesh = new THREE.Mesh(geom, material);
        mesh.rotation.x = 0.4;
        scene.add(mesh);

        // Subtle hover / focus scaling when page is focused (non-interactive)
        let hoverScale = 1;
        function onFocus() { hoverScale = 1.06; container.style.transform = 'translateY(-6px) scale(1.02)'; }
        function onBlur() { hoverScale = 1; container.style.transform = 'none'; }
        window.addEventListener('focus', onFocus);
        window.addEventListener('blur', onBlur);

        // Parallax from cursor but do not capture pointer events (read-only)
        let mouseX = 0, mouseY = 0;
        window.addEventListener('mousemove', (e) => {
            const rect = container.getBoundingClientRect();
            mouseX = (e.clientX - (rect.left + rect.width / 2)) / rect.width;
            mouseY = (e.clientY - (rect.top + rect.height / 2)) / rect.height;
        }, { passive: true });

        function resize() {
            const w = container.clientWidth || size;
            const h = container.clientHeight || size;
            renderer.setSize(w, h, false);
            camera.aspect = w / h;
            camera.updateProjectionMatrix();
        }

        function animate() {
            requestAnimationFrame(animate);
            mesh.rotation.y += 0.0065;
            mesh.rotation.x += 0.0028;
            // gentle parallax
            mesh.position.x += (mouseX * 6 - mesh.position.x) * 0.06;
            mesh.position.y += (-mouseY * 6 - mesh.position.y) * 0.06;
            mesh.scale.setScalar(hoverScale * (1 + Math.sin(Date.now() * 0.001) * 0.007));
            renderer.render(scene, camera);
        }

        window.addEventListener('resize', resize);
        resize();
        animate();
    } catch (e) {
        console.warn('three-bg init failed', e);
    }
})();
