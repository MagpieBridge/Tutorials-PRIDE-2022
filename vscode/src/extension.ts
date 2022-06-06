'use strict';
import * as net from 'net';
import * as path from 'path';
import { XMLHttpRequest } from 'xmlhttprequest-ts';
import { workspace, window, ExtensionContext, ViewColumn, TaskPanelKind} from 'vscode';
import { ClientCapabilities, DocumentSelector, DynamicFeature, InitializeParams, LanguageClient, LanguageClientOptions, RegistrationData, RPCMessageType, ServerCapabilities, ServerOptions, StreamInfo } from 'vscode-languageclient';

export function activate(context: ExtensionContext) {
    let script = 'java';
    let args = ['-jar', context.asAbsolutePath(path.join('tutorials-0.0.2-SNAPSHOT.jar'))];
   /* let serverOptions: ServerOptions = {
        run : { command: script, args: args },
        debug: { command: script, args: args} //, options: { env: createDebugEnv() }
    };
    */
    //   Use this for debugging 
   let serverOptions = () => {
		const socket = net.connect({ port: 5007 })
		const result: StreamInfo = {
			writer: socket,
			reader: socket
		}
		return new Promise<StreamInfo>((resolve) => {
			socket.on("connect", () => resolve(result))
			socket.on("error", _ =>
				window.showErrorMessage(
					"Failed to connect to the language server. Make sure that the language server is running " +
					"-or- configure the extension to connect via standard IO."))
		})
    }
    
    let clientOptions: LanguageClientOptions = {
        documentSelector: [{ scheme: 'file', language: 'java' }],
        synchronize: {
            configurationSection: 'java',
            fileEvents: [ workspace.createFileSystemWatcher('**/*.java') ]
        }
    };
    
    // Create the language client and start the client.
    let lc : LanguageClient = new LanguageClient('HelloWorld','HelloWorld', serverOptions, clientOptions);
    lc.registerFeature(new MagpieBridgeSupport(lc));
    lc.start();
}

export class MagpieBridgeSupport implements DynamicFeature<undefined>{
    constructor(private _client: LanguageClient) { }
    messages: RPCMessageType | RPCMessageType[];
    fillInitializeParams?: (params: InitializeParams) => void;
    fillClientCapabilities(capabilities: ClientCapabilities): void {
        capabilities.experimental = {
            supportsShowHTML : false
        }
    }
    initialize(capabilities: ServerCapabilities, documentSelector: DocumentSelector): void {
        let client = this._client;
        client.onNotification("magpiebridge/showHTML", (content: string) => {
            this.createWebView(content);
        });
    }
    createWebView(content: string) {
        let panel = window.createWebviewPanel("Customized Web View","MagpieBridge", ViewColumn.Beside, {
            retainContextWhenHidden: true,
            enableScripts: true
        });
        panel.webview.html = content;
        panel.webview.onDidReceiveMessage(
            message => {
                switch (message.command) {
                    case 'action':
                        var httpRequest = new XMLHttpRequest();
                        var url = message.text;
                        httpRequest.open('GET', url);
                        httpRequest.send();
                        return;
                    case 'configuration':
                        var httpRequest = new XMLHttpRequest();
                        var splits = message.text.splits("?");
                        var url = splits[0];
                        var formData = splits[1];
                        httpRequest.open('POST', url);
                        httpRequest.send(formData);
                        return; 
                }

            }
        )    
    }
    register(message: RPCMessageType, data: RegistrationData<undefined>): void {
       
    }
    unregister(id: string): void {

    }
    dispose(): void {

    }

}

