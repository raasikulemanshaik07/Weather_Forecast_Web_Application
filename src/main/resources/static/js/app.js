document.addEventListener('DOMContentLoaded', function () {

    const searchInput = document.getElementById('cityInput');
    if (searchInput) {
        searchInput.focus();
    }

    const searchForm = document.getElementById('searchForm');
    if (searchForm) {
        searchForm.addEventListener('submit', function (e) {
            const city = searchInput.value.trim();
            if (!city) {
                e.preventDefault();
                searchInput.classList.add('shake');
                setTimeout(function () {
                    searchInput.classList.remove('shake');
                }, 500);
                return;
            }
            var btn = document.getElementById('searchBtn');
            btn.innerHTML = '<span class="spinner"></span> Searching...';
            btn.disabled = true;
        });
    }

    var observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    var observer = new IntersectionObserver(function (entries) {
        entries.forEach(function (entry) {
            if (entry.isIntersecting) {
                entry.target.classList.add('animate-in');
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);

    document.querySelectorAll('.forecast-card').forEach(function (card) {
        observer.observe(card);
    });

    var weatherMain = document.querySelector('.weather-main');
    if (weatherMain) {
        setTimeout(function () {
            weatherMain.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }, 300);
    }

    updateTime();
    setInterval(updateTime, 60000);
});

function updateTime() {
    var timeEl = document.getElementById('currentTime');
    if (timeEl) {
        var now = new Date();
        var options = {
            weekday: 'long',
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        };
        timeEl.textContent = now.toLocaleDateString('en-US', options);
    }
}
