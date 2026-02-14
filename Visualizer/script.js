const TILE_SIZE = 96;
const boardEl = document.getElementById("board");
const reloadBtn = document.getElementById("reload");

/* ===============================
   TILES BASE (0°)
   =============================== */

const BASE_TILES = [
	{
		id: "tile_01_mountain_range",
		sides: ["MOUNTAIN_RANGE_IN", "MOUNTAIN_RANGE_OUT", "EMPTY", "EMPTY"],
	},
	{ id: "tile_02_river_straight", sides: ["RIVER", "EMPTY", "RIVER", "EMPTY"] },
	{ id: "tile_03_river_corner", sides: ["RIVER", "RIVER", "EMPTY", "EMPTY"] },
	{ id: "tile_04_mountain_end", sides: ["MOUNTAIN", "EMPTY", "EMPTY", "EMPTY"] },
	{ id: "tile_05_river_mountain", sides: ["RIVER", "MOUNTAIN", "RIVER", "EMPTY"] },
	{ id: "tile_06_river_corner_mountain", sides: ["RIVER", "RIVER", "EMPTY", "MOUNTAIN"] },
	{ id: "tile_07_empty", sides: ["EMPTY", "EMPTY", "EMPTY", "EMPTY"] },
];

/* ===============================
   UTILIDADES
   =============================== */

function rotateSides([n, e, s, w]) {
	return [w, n, e, s];
}

function resolveTileSprite(tile) {
	const target = [tile.north, tile.east, tile.south, tile.west];

	for (const base of BASE_TILES) {
		let rotated = [...base.sides];

		for (let i = 0; i < 4; i++) {
			if (rotated.every((v, idx) => v === target[idx])) {
				return {
					sprite: base.id,
					rotation: i * 90,
				};
			}
			rotated = rotateSides(rotated);
		}
	}

	console.warn("Tile no reconocido:", tile);
	return { sprite: "tile_07_empty", rotation: 0 };
}

/* ===============================
   RENDER DEL TABLERO
   =============================== */

function renderBoard(data) {
	boardEl.innerHTML = "";

	boardEl.style.gridTemplateColumns = `repeat(${data.cols}, ${TILE_SIZE}px)`;
	boardEl.style.gridTemplateRows = `repeat(${data.rows}, ${TILE_SIZE}px)`;

	for (let r = 0; r < data.rows; r++) {
		for (let c = 0; c < data.cols; c++) {
			const tileData = data.tiles[r][c];
			const { sprite, rotation } = resolveTileSprite(tileData);

			const tile = document.createElement("div");
			tile.className = "tile";
			tile.style.backgroundImage = `url("sprites/${sprite}.png")`;
			tile.style.transform = `rotate(${rotation}deg)`;

			boardEl.appendChild(tile);
		}
	}
}

/* ===============================
   CARGA DE BOARD ALEATORIO
   =============================== */

let allBoards = [];

async function loadBoards() {
	try {
		const response = await fetch("validBoards.json");
		if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
		allBoards = await response.json();
		loadRandomBoard();
	} catch (e) {
		console.error("No se pudo cargar el JSON:", e);
		boardEl.innerHTML = `<p style="color:red">Error al cargar validBoards.json. Revisa consola.</p>`;
	}
}

function loadRandomBoard() {
	if (!allBoards.length) return;

	const randomIndex = Math.floor(Math.random() * allBoards.length);
	renderBoard(allBoards[randomIndex]);
}

/* ===============================
   INIT
   =============================== */

reloadBtn.addEventListener("click", loadRandomBoard);
loadBoards();
