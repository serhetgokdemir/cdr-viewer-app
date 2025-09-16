/*
*   Written by Serhet Gökdemir in 12.09.2025
 */

// Secili dosya ve klasor bilgileri
let selectedFile = null;   // kullanici tarafindan secilen dosya
let selectedFolder = null; // dosyanin bulundugu klasor

// Lazy loading icin sayaclar
let currentStart = 0;      // su ana kadar yuklenen blok sayisi
const pageSize = 2;        // her scroll icin yüklenecek blok sayisi (PC'ye gore degistirilebilir)
let loading = false;       // scroll sirasinda yeni chunk yuklenirken true olur

// Arama sonucu cache
let lastSearchResult = null;  // son arama sonucu
let mode = 'file';            // 'file' = dosya gosterim, 'search' = arama sonucu gosterim
let searchRenderIndex = 0;    // arama sonucunda gosterilen blok sayaci

/**
 * Gelen CdrBlock listesi icin HTML olusturur ve resultsDiv'e ekler.
 */
function renderCdrBlocks(data, resultsDiv) {
    data.forEach(block => {
        let div = document.createElement("div");
        div.className = "block";

        // Satirlari ekler
        block.lines.forEach(line => {
            let lineDiv = document.createElement("div");
            lineDiv.innerText = "  " + line;
            div.appendChild(lineDiv);
        });

        // Ayirici
        let separator = document.createElement("div");
        separator.style.margin = "10px 0";
        div.appendChild(separator);

        resultsDiv.appendChild(div);
    });
}

/**
 * Arama sonucunu parca parca (lazy) render eder.
 */
function renderNextSearchChunk(resultsDiv) {
    if (!lastSearchResult) return;
    if (searchRenderIndex >= lastSearchResult.length) return;

    const slice = lastSearchResult.slice(searchRenderIndex, searchRenderIndex + pageSize);
    renderCdrBlocks(slice, resultsDiv);
    searchRenderIndex += slice.length;
}

// -------------------- Search form --------------------
document.getElementById("searchForm").addEventListener("submit", async (e) => {
    e.preventDefault();

    if (!selectedFile) {
        document.getElementById("results").innerHTML = "Once bir dosya secin!";
        return;
    }

    let key = document.getElementById("key").value;
    let value = document.getElementById("value").value;
    let resultsDiv = document.getElementById("results");
    let folder = document.getElementById("path").value;

    // Sonuc gelene kadar bekleme yazisi
    document.getElementById("resultCount").innerText = "Yukleniyor...";

    try {
        const response = await fetch(`/api/search?folder=${encodeURIComponent(folder)}&name=${encodeURIComponent(selectedFile)}&key=${encodeURIComponent(key)}&value=${encodeURIComponent(value)}`);

        if (!response.ok) {
            const error = await response.json();
            resultsDiv.innerHTML = `[HATA ${error.status}] ${error.message}`;
            saveBtn.disabled = true;
            mode = 'file';
            return;
        }

        const data = await response.json();
        lastSearchResult = data;
        resultsDiv.innerHTML = "";

        const countDiv = document.getElementById("resultCount");

        if (data.length === 0) {
            countDiv.innerText = "Eslesen kayit bulunamadi.";
            resultsDiv.innerHTML = "Eslesen kayit bulunamadi.";
            saveBtn.disabled = true;
            return;
        }

        // Toplam sonuc sayisini goster
        countDiv.innerText = `Eslesen ${data.length} CDR bulundu.`;

        // Arama moduna gec ve ilk parcayi render et
        mode = 'search';
        searchRenderIndex = 0;
        renderNextSearchChunk(resultsDiv);

        saveBtn.disabled = false;
        clearBtn.disabled = false;

    } catch (err) {
        resultsDiv.innerHTML = "Sunucuya baglanirken bir hata olustu!";
        saveBtn.disabled = true;
        mode = 'file';
    }
});

// -------------------- Folder ekleme formu --------------------
const form = document.querySelector("#uploadForm");
if (form) {
    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const path = document.getElementById("path").value;

        try {
            const response = await fetch(`/api/addPath?path=${encodeURIComponent(path)}`, {
                method: "POST"
            });

            if (!response.ok) {
                const error = await response.text();
                document.getElementById("results").innerHTML = error;
                return;
            }

            const files = await response.json();
            const fileList = document.getElementById("fileList");
            fileList.innerHTML = "";

            files.forEach(name => {
                const li = document.createElement("li");
                li.textContent = name;

                // Dosya secildiginde guncelle
                li.addEventListener("click", async () => {
                    selectedFile = name;

                    document.querySelectorAll("#fileList li").forEach(el => el.classList.remove("selected"));
                    li.classList.add("selected");

                    await loadFileContent(name);
                });

                fileList.appendChild(li);
            });

        } catch (err) {
            document.getElementById("results").innerHTML = "Hata: " + err;
        }
    });
}

// -------------------- File content yukleme --------------------
async function loadFullFileContent(name) {
    try {
        const folder = document.getElementById("path").value;
        const response = await fetch(`/api/file?folder=${encodeURIComponent(folder)}&name=${encodeURIComponent(name)}`);
        const data = await response.json();

        const resultsDiv = document.getElementById("results");
        resultsDiv.innerHTML = "";
        renderCdrBlocks(data, resultsDiv);

    } catch (err) {
        document.getElementById("results").innerHTML = "Dosya okunamadi!";
    }
}

async function loadFileContent(name) {
    mode = 'file';
    lastSearchResult = null;
    searchRenderIndex = 0;

    currentStart = 0;
    selectedFile = name;
    selectedFolder = document.getElementById("path").value;

    document.getElementById("results").innerHTML = "";
    await loadNextChunk();
}

// Scroll eventi ile lazy loading
document.getElementById("results").addEventListener("scroll", async () => {
    const resultsDiv = document.getElementById("results");

    if (!loading && resultsDiv.scrollTop + resultsDiv.clientHeight >= resultsDiv.scrollHeight - 5) {
        if (mode === 'search') {
            if (lastSearchResult && searchRenderIndex < lastSearchResult.length) {
                loading = true;
                renderNextSearchChunk(resultsDiv);
                loading = false;
            }
        } else {
            await loadNextChunk();
        }
    }
});

// Dosyanin bir sonraki chunk'ini yukler
async function loadNextChunk() {
    loading = true;
    try {
        const response = await fetch(`/api/file/chunk?folder=${encodeURIComponent(selectedFolder)}&name=${encodeURIComponent(selectedFile)}&start=${currentStart}&lines=${pageSize}`);
        const data = await response.json();

        const resultsDiv = document.getElementById("results");
        renderCdrBlocks(data, resultsDiv);

        if (data.length > 0) {
            currentStart += pageSize;
        }
    } catch (err) {
        console.error("Lazy loading hatasi:", err);
    }
    loading = false;
}

// -------------------- Save button --------------------
const saveBtn = document.getElementById("saveBtn");
if (saveBtn) {
    saveBtn.addEventListener("click", () => {
        if (!lastSearchResult || lastSearchResult.length === 0) {
            alert("Kaydedilecek arama sonucu yok!");
            return;
        }

        let allText = "";
        lastSearchResult.forEach(block => {
            allText += block.fileName + "\n{\n";
            block.lines.forEach(line => {
                allText += "  " + line + "\n";
            });
            allText += "}\n\n";
        });

        const blob = new Blob([allText], { type: "text/plain" });
        const url = URL.createObjectURL(blob);

        const a = document.createElement("a");
        a.href = url;
        a.download = "results.txt";
        a.click();

        URL.revokeObjectURL(url);
    });
}

// -------------------- Clear button --------------------
const clearBtn = document.getElementById("clearBtn");
if (clearBtn) {
    clearBtn.disabled = false;
    clearBtn.addEventListener("click", async () => {
        document.getElementById("resultCount").innerText = "";
        document.getElementById("results").innerHTML = "";
        document.getElementById("key").value = "";
        document.getElementById("value").value = "";


        mode = 'file';
        lastSearchResult = null;
        searchRenderIndex = 0;

        currentStart = 0;
        if (selectedFile && selectedFolder) {
            await loadNextChunk();
        }
    });
}