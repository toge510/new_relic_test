<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="true" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Weather App</title>
    <style>
        body { font-family: Arial, Helvetica, sans-serif; background:#f4f7fb; color:#222; margin:0; padding:20px; }
        .container { max-width:720px; margin:24px auto; background:#fff; padding:18px 22px; border-radius:8px; box-shadow:0 6px 18px rgba(20,30,60,0.08); }
        h1 { margin-top:0; }
        form { display:flex; gap:8px; align-items:center; margin-bottom:12px; }
        input[type="text"] { flex:1; padding:8px 10px; border:1px solid #ccd6e0; border-radius:4px; }
        button { padding:8px 12px; border:0; background:#0066cc; color:#fff; border-radius:4px; cursor:pointer; }
        button:disabled { opacity:0.6; cursor:not-allowed; }
        .card { display:flex; gap:16px; align-items:center; padding:12px; border-radius:6px; background:linear-gradient(180deg,#ffffff,#fbfdff); border:1px solid #e6eef8; }
        .temp { font-size:48px; font-weight:700; }
        .meta { color:#53617a; }
        .small { font-size:14px; color:#6b778c; }
        .icon { width:96px; height:96px; }
        .error { color:#b00020; }
        .row { display:flex; gap:16px; margin-top:8px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Weather App</h1>
        <form id="weather-form" action="/weather" method="get">
            <label for="city" class="small">City</label>
            <input type="text" id="city" name="city" placeholder="e.g. Tokyo" required />
            <button id="get-weather">Get Weather</button>
        </form>

        <div id="weather-result" aria-live="polite"></div>
    </div>

    <script>
        const form = document.getElementById('weather-form');
        const result = document.getElementById('weather-result');
        const btn = document.getElementById('get-weather');

        form.addEventListener('submit', async (e) => {
            e.preventDefault();
            const city = document.getElementById('city').value.trim();
            if (!city) return;
            result.innerHTML = '';
            btn.disabled = true;
            btn.textContent = 'Loading...';

            try {
                const resp = await fetch(`/weather?city=${encodeURIComponent(city)}`);
                if (!resp.ok) {
                    const text = await resp.text();
                    result.innerHTML = `<div class="error">${escapeHtml(text || 'Could not fetch weather')}</div>`;
                    return;
                }

                const data = await resp.json();
                renderWeather(data);
            } catch (err) {
                result.innerHTML = `<div class="error">Network error: ${escapeHtml(err.message)}</div>`;
            } finally {
                btn.disabled = false;
                btn.textContent = 'Get Weather';
            }
        });

        function renderWeather(data) {
            if (!data || data.cod && data.cod !== 200) {
                const msg = data && data.message ? data.message : 'No data';
                result.innerHTML = `<div class="error">${escapeHtml(msg)}</div>`;
                return;
            }

            const name = data.name || '';
            const country = (data.sys && data.sys.country) ? data.sys.country : '';
            const temp = data.main && typeof data.main.temp !== 'undefined' ? Math.round(data.main.temp) : '--';
            const feels = data.main && typeof data.main.feels_like !== 'undefined' ? Math.round(data.main.feels_like) : '--';
            const humidity = data.main && typeof data.main.humidity !== 'undefined' ? data.main.humidity : '--';
            const wind = data.wind && typeof data.wind.speed !== 'undefined' ? data.wind.speed : '--';
            const weather = Array.isArray(data.weather) && data.weather[0] ? data.weather[0] : null;
            const desc = weather ? weather.description : '';
            const icon = weather ? weather.icon : '';

            const iconUrl = icon ? `https://openweathermap.org/img/wn/${icon}@2x.png` : '';

            result.innerHTML = `
                <div class="card">
                    ${iconUrl ? `<img class="icon" src="${iconUrl}" alt="${escapeHtml(desc)}" />` : ''}
                    <div>
                        <div style="display:flex;align-items:baseline;gap:12px;">
                            <div class="temp">${escapeHtml(String(temp))}&deg;C</div>
                            <div>
                                <div style="font-weight:600">${escapeHtml(name)} ${escapeHtml(country)}</div>
                                <div class="meta">${escapeHtml(desc)}</div>
                            </div>
                        </div>
                        <div class="row small">
                            <div>Feels like: <strong>${escapeHtml(String(feels))}&deg;C</strong></div>
                            <div>Humidity: <strong>${escapeHtml(String(humidity))}%</strong></div>
                            <div>Wind: <strong>${escapeHtml(String(wind))} m/s</strong></div>
                        </div>
                    </div>
                </div>
            `;
        }

        // very small helper to avoid injecting raw text
        function escapeHtml(s) {
            return String(s)
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#039;');
        }
    </script>
</body>
</html>