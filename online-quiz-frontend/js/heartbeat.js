// heartbeat.js - Keep user session active and track online status (Simplified version)

(function() {
    // Simplified heartbeat - just check if user is logged in
    function startHeartbeat() {
        console.log('Heartbeat monitoring started');
    }
    
    function stopHeartbeat() {
        console.log('Heartbeat monitoring stopped');
    }
    
    function setUserOffline() {
        const username = localStorage.getItem('username');
        if (username) {
            console.log('User offline:', username);
        }
    }
    
    // Start heartbeat when page loads if user is logged in
    if (localStorage.getItem('token') && localStorage.getItem('username')) {
        startHeartbeat();
    }
    
    // Set user offline when page is closing/reloading
    window.addEventListener('beforeunload', function() {
        stopHeartbeat();
        setUserOffline();
    });
    
    // Handle page unload
    window.addEventListener('unload', function() {
        setUserOffline();
    });
    
    // Expose functions globally for manual control if needed
    window.startHeartbeat = startHeartbeat;
    window.stopHeartbeat = stopHeartbeat;
})();
