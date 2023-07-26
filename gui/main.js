const {app, BrowserWindow} = require('electron')
const path = require('path')
const url = require('url')

function createWindow() {
    const win = new BrowserWindow({
        width: 1024,
        height: 768,
        webPreferences: {
            nodeIntegration: true,
            contextIsolation: false,
        }
    })

    const startUrl = process.env.ELECTRON_START_URL || url.format({
        pathname: path.join(__dirname, '/dist/index.html'),
        protocol: 'file:',
        slashes: true
    });

    win.loadURL(startUrl)
    win.setMenu(null)
}

app.whenReady().then(createWindow)
