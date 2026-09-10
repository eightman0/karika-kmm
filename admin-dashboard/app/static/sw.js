// Deliberately does no caching - this is an ops tool for a live device fleet, where an admin
// silently acting on stale cached data would be worse than no offline support at all. Its only
// job is to exist with a fetch handler, since that is one of the criteria Chrome/Android checks
// before treating this site as installable.
self.addEventListener("fetch", (event) => {
  event.respondWith(fetch(event.request));
});
