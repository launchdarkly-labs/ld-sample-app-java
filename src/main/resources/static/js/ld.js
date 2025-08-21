const context = {
    "kind": "multi",
    "user": {
        "key": "user-018e7bd4-ab96-782e-87b0-b1e32082b481",
        "name": "Miriam Wilson",
        "language": "en",
        "tier": "premium",
        "userId": "mwilson",
        "role": "developer",
        "email": "miriam.wilson@example.com",
    },
    "device": {
        "key": "device-018e7bd4-ab96-782e-87b0-b1e32082b481",
        "os": "macOS",
        "osVersion": "15.6",
        "model": "MacBook Pro",
        "manufacturer": "Apple",
    },
};

const client = LDClient.initialize(clientKey, context);

client.on('ready', () => {
    // initialization succeeded, flag values are now available
    const flagValue = client.variation('release-home-page-slider', false);
    // etc.
});

client.on('change', (settings) => {
    console.log(settings);
    if (settings['release-home-page-slider']) {
        switchSlider(settings['release-home-page-slider'].current);
    }
    if (settings['coffee-promo-1']) {
        switchPromo(settings['coffee-promo-1'].current, 1);
    }
    if (settings['coffee-promo-2']) {
        switchPromo(settings['coffee-promo-2'].current, 2);
    }
    switchSection();
})

function switchSlider(enabled) {
    slider = document.getElementById('home-page-slider');
    if (enabled) {
        slider.style.display = 'block';
    } else {
        slider.style.display = 'none';
    }
}

function switchPromo(enabled, id) {
    promo = document.getElementById('coffee-promo-' + id);
    if (enabled) {
        promo.style.display = 'block';
    } else {
        promo.style.display = 'none';
    }
}

function switchSection() {
    section = document.getElementById('coffee-promo-section');
    promo1 = document.getElementById('coffee-promo-1');
    promo2 = document.getElementById('coffee-promo-2');
    if (promo1.style.display !== 'none' || promo2.style.display !== 'none') {
        section.style.display = 'block';
    } else {
        section.style.display = 'none';
    }
}

function getBannerText() {
    fetch('/api/banner')
        .then(response => response.json())
        .then(data => {
            const bannerText = data.primaryBanner;
            const heroBanner = document.getElementById('hero-banner');
            heroBanner.textContent = bannerText;
            console.log('Banner text updated:', bannerText);
        })
        .catch(error => {
            console.error('Error fetching banner text:', error);
        });
}