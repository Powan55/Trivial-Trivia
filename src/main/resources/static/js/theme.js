// This file exists twice: src/main/resources/static/js/ and site/. Pages has no server to share it, so keep them identical.
(function () {
    var root = document.documentElement;
    var button = document.getElementById('theme-toggle');
    if (!button) {
        return;
    }

    function store(theme) {
        try {
            localStorage.setItem('theme', theme);
        } catch (error) {
            // Private mode, or storage full. The page still works, the choice just will not stick.
        }
    }

    function apply(theme) {
        root.setAttribute('data-theme', theme);
        button.setAttribute('aria-pressed', String(theme === 'dark'));
    }

    apply(root.getAttribute('data-theme') === 'dark' ? 'dark' : 'light');
    button.hidden = false;

    button.addEventListener('click', function () {
        var next = root.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
        apply(next);
        store(next);
    });
})();
