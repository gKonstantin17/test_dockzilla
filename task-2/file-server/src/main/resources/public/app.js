// DOM Elements
const uploadForm = document.getElementById('uploadForm');
const fileInput = document.getElementById('fileInput');
const fileName = document.getElementById('fileName');
const uploadBtn = document.getElementById('uploadBtn');
const progressSection = document.getElementById('progressSection');
const progressFill = document.getElementById('progressFill');
const progressText = document.getElementById('progressText');
const resultSection = document.getElementById('resultSection');
const downloadLink = document.getElementById('downloadLink');
const copyBtn = document.getElementById('copyBtn');
const newUploadBtn = document.getElementById('newUploadBtn');
const errorSection = document.getElementById('errorSection');
const errorMessage = document.getElementById('errorMessage');

// Stats Elements
const totalFiles = document.getElementById('totalFiles');
const totalSize = document.getElementById('totalSize');
const filesTableBody = document.getElementById('filesTableBody');

// File Input Change Handler
fileInput.addEventListener('change', (e) => {
    const file = e.target.files[0];
    if (file) {
        fileName.textContent = file.name;
    } else {
        fileName.textContent = 'Выберите файл';
    }
});

// Upload Form Submit Handler
uploadForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    const file = fileInput.files[0];
    if (!file) {
        showError('Пожалуйста, выберите файл');
        return;
    }

    await uploadFile(file);
});

// Upload File Function
async function uploadFile(file) {
    hideAll();
    showProgress();

    const formData = new FormData();
    formData.append('file', file);

    try {
        const xhr = new XMLHttpRequest();

        xhr.upload.addEventListener('progress', (e) => {
            if (e.lengthComputable) {
                const percentComplete = (e.loaded / e.total) * 100;
                updateProgress(percentComplete);
            }
        });

        xhr.addEventListener('load', () => {
            if (xhr.status === 200) {
                const response = JSON.parse(xhr.responseText);
                const fullUrl = window.location.origin + response.downloadUrl;
                showResult(fullUrl);
                loadStats();
            } else {
                showError('Ошибка при загрузке файла');
            }
        });

        xhr.addEventListener('error', () => {
            showError('Ошибка соединения');
        });

        xhr.open('POST', '/api/upload');
        xhr.send(formData);

    } catch (error) {
        showError('Ошибка: ' + error.message);
    }
}

// Copy Link Handler
copyBtn.addEventListener('click', () => {
    downloadLink.select();
    document.execCommand('copy');

    const originalText = copyBtn.textContent;
    copyBtn.textContent = 'Скопировано!';
    setTimeout(() => {
        copyBtn.textContent = originalText;
    }, 2000);
});

// New Upload Handler
newUploadBtn.addEventListener('click', () => {
    resetForm();
});

// Load Statistics
async function loadStats() {
    try {
        const response = await fetch('/api/stats');
        const data = await response.json();

        totalFiles.textContent = data.totalFiles;
        totalSize.textContent = formatBytes(data.totalSize);

        renderFilesTable(data.files);
    } catch (error) {
        console.error('Failed to load stats:', error);
    }
}

// Render Files Table
function renderFilesTable(files) {
    if (files.length === 0) {
        filesTableBody.innerHTML = '<tr><td colspan="5" class="no-data">Нет файлов</td></tr>';
        return;
    }

    filesTableBody.innerHTML = files.map(file => `
        <tr>
            <td>${escapeHtml(file.originalFilename)}</td>
            <td>${formatBytes(file.size)}</td>
            <td>${formatDate(file.uploadTime)}</td>
            <td>${file.downloadCount}</td>
            <td>${formatDate(file.lastAccessTime)}</td>
        </tr>
    `).join('');
}

// Utility Functions
function showProgress() {
    progressSection.classList.remove('hidden');
    uploadBtn.disabled = true;
}

function updateProgress(percent) {
    progressFill.style.width = percent + '%';
    progressText.textContent = `Загрузка... ${Math.round(percent)}%`;
}

function showResult(url) {
    progressSection.classList.add('hidden');
    resultSection.classList.remove('hidden');
    downloadLink.value = url;
}

function showError(message) {
    hideAll();
    errorSection.classList.remove('hidden');
    errorMessage.textContent = message;
    uploadBtn.disabled = false;
}

function hideAll() {
    progressSection.classList.add('hidden');
    resultSection.classList.add('hidden');
    errorSection.classList.add('hidden');
}

function resetForm() {
    uploadForm.reset();
    fileName.textContent = 'Выберите файл';
    hideAll();
    uploadBtn.disabled = false;
}

function formatBytes(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
}

function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleString('ru-RU', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
    });
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Load stats on page load
document.addEventListener('DOMContentLoaded', () => {
    loadStats();
    // Refresh stats every 30 seconds
    setInterval(loadStats, 30000);
});
