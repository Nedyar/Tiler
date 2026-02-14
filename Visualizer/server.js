const http = require("http");
const fs = require("fs");
const path = require("path");

const PORT = 8080;
const PUBLIC_DIR = path.join(__dirname);

const mimeTypes = {
	".html": "text/html",
	".js": "application/javascript",
	".css": "text/css",
	".json": "application/json",
	".png": "image/png",
};

const server = http.createServer((req, res) => {
	let filePath = path.join(PUBLIC_DIR, req.url === "/" ? "index.html" : req.url);

	fs.readFile(filePath, (err, content) => {
		if (err) {
			res.writeHead(404);
			res.end("Not found");
			return;
		}
		const ext = path.extname(filePath);
		res.writeHead(200, { "Content-Type": mimeTypes[ext] || "application/octet-stream" });
		res.end(content);
	});
});

server.listen(PORT, () => {
	console.log(`Servidor escuchando en http://localhost:${PORT}`);
});
