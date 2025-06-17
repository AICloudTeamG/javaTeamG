const weatherData = [
    { label: '三日前', tenki: '☀️', temp: '22°C', wind: '4 m/s', offset: -3 },
    { label: '一昨日', tenki: '☔️', temp: '20°C', wind: '7 m/s', offset: -2 },
    { label: '昨日', tenki: '☁️', temp: '23°C', wind: '5 m/s', offset: -1 },
    { label: '今日', tenki: '☀️', temp: '26°C', wind: '4 m/s', offset: 0 },
    { label: '明日', tenki: '☁️', temp: '24°C', wind: '3 m/s', offset: 1 },
    { label: '明後日', tenki: '☔️', temp: '21°C', wind: '6 m/s', offset: 2 },
    { label: '大後日', tenki: '☀️', temp: '25°C', wind: '2 m/s', offset: 3 }
];

let weatherIndex = 3; // '今日'から開始

document.addEventListener('DOMContentLoaded', function() {
    // 販売日付の初期値を今日の日付に設定
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('recordDate').value = today;

    // 天気ポップアップの初期表示を更新
    updateWeatherContent();

    // 登録ボタンの活性化ロジック (例: すべての数値入力フィールドに値が入ったら活性化)
    // このロジックはバックエンドの要件に合わせて調整してください
    const salesInputs = document.querySelectorAll('table input[type="number"]');
    const registerButton = document.querySelector('.button.primary-button');

    function checkInputs() {
        let allFilled = true;
        salesInputs.forEach(input => {
            // 値が入力されているか、または空（未入力）でないか
            if (input.value === '' || input.value === null || parseInt(input.value) < 0) {
                allFilled = false;
            }
        });
        // 全て入力されている場合にボタンを活性化
        if (allFilled) {
            registerButton.disabled = false;
            registerButton.classList.remove('disabled-button'); // CSSでdisabledを表現するためのクラス
        } else {
            registerButton.disabled = true;
            registerButton.classList.add('disabled-button');
        }
    }

    salesInputs.forEach(input => {
        input.addEventListener('input', checkInputs);
    });

    checkInputs(); // ページロード時にもチェック
});

function toggleWeatherPopup() {
    const popup = document.getElementById('weatherPopup');
    popup.style.display = popup.style.display === 'block' ? 'none' : 'block';
}

function getShortWeekday(dateObj) {
    const days = ['日', '月', '火', '水', '木', '金', '土'];
    return days[dateObj.getDay()];
}

function updateWeatherContent() {
    const data = weatherData[weatherIndex];
    const date = new Date();
    date.setDate(date.getDate() + data.offset);
    const month = date.getMonth() + 1;
    const day = date.getDate();
    const weekday = getShortWeekday(date);
    const content = document.getElementById('weatherContent');
    content.innerHTML = `<strong>${data.label}の天気情報</strong><br><span style="font-size: 0.85rem; color: #555;">${month}/${day}（${weekday}）</span><br>${data.tenki}<br>気温：${data.temp}<br>風速：${data.wind}`;
}

function changeWeather(direction) {
    weatherIndex += direction;
    if (weatherIndex < 0) weatherIndex = 0;
    if (weatherIndex >= weatherData.length) weatherIndex = weatherData.length - 1; // 修正: 配列の範囲を考慮
    updateWeatherContent();
}