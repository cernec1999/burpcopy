package com.ccerne.burpcopy

import burp.api.montoya.BurpExtension
import burp.api.montoya.MontoyaApi
import burp.api.montoya.ui.contextmenu.ContextMenuEvent
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider
import com.github.jknack.handlebars.EscapingStrategy
import com.github.jknack.handlebars.Handlebars
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import javax.swing.JMenuItem

class BurpCopyEntry : BurpExtension {
    override fun initialize(api: MontoyaApi?) {
        if (api == null) {
            return
        }

        /* Set the Singleton that the whole extension can reference */
        Utils.montoyaApi = api

        Utils.montoyaApi.extension().setName(EXTENSION_NAME)
        Utils.logDebug("Loading $EXTENSION_NAME")

        Utils.montoyaApi.userInterface().registerContextMenuItemsProvider(object : ContextMenuItemsProvider {
            override fun provideMenuItems(event: ContextMenuEvent?): List<JMenuItem> {
                val selectedReqResp = event?.selectedRequestResponses()
                val msgEditorReqResp = event?.messageEditorRequestResponse()

                val actualReqResp = if (msgEditorReqResp?.isPresent == true) {
                    listOf(msgEditorReqResp.get().requestResponse())
                } else {
                    selectedReqResp ?: listOf()
                }

                if (actualReqResp.isEmpty())
                    return emptyList()

                val reqRespJMenuItem = JMenuItem("Copy Request / Response Pair")
                reqRespJMenuItem.addActionListener {
                    /* Get system clipboard */
                    //val htmlSelection = RequestResponseClipboardData(actualReqResp)
                    val htmlSelection = object : Transferable {
                        override fun getTransferDataFlavors(): Array<DataFlavor> {
                            return arrayOf(DataFlavor.allHtmlFlavor)
                        }

                        override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean {
                            return true
                        }

                        override fun getTransferData(flavor: DataFlavor?): Any {
                            if (flavor != DataFlavor.allHtmlFlavor)
                                return ""

                            try {
                                //actualReqResp[0].request().headers()
                                val newReqResp = actualReqResp.map {
                                    arrayOf(
                                        BurpCopyHttpMessage(
                                            Utils.encodeHtml(it.request().method()),
                                            it.request().headers().associate { it.name() to Utils.encodeHtml(it.value()) },
                                            Utils.encodeHtml(it.request().path()),
                                            Utils.encodeHtml(it.request().bodyToString()),
                                            it.request().httpVersion(),
                                            0,
                                            null
                                        ),
                                        BurpCopyHttpMessage(
                                            null,
                                            it.response().headers().associate { it.name() to Utils.encodeHtml(it.value()) },
                                            null,
                                            Utils.encodeHtml(it.response().bodyToString()),
                                            it.response().httpVersion(),
                                            it.response().statusCode(),
                                            it.response().reasonPhrase()
                                        ),
                                    )
                                }
                                val handlebars = Handlebars()
                                    .with(EscapingStrategy.NOOP)
                                val template = handlebars.compileInline("""
                                    {{#each this}}
                                    <p style="line-height: 1; margin-top: 0; margin-bottom: 0;"><b>Request</b></p>
                                    <div>
                                        <table style="font-family: 'Courier New', monospace;">
                                            <tbody>
                                                <tr>
                                                    <td style="line-height: 1; margin-top: 0; margin-bottom: 0;">
                                                        <p style="line-height: 1; margin-top: 0; margin-bottom: 0;">{{this.[0].method}} {{this.[0].path}} {{this.[0].httpVersion}}</p>
                                                        {{#each this.[0].headers}}
                                                        <p style="line-height: 1; margin-top: 0; margin-bottom: 0;">{{@key}}: {{this}}</p>
                                                        {{/each}}
                                                        <br />
                                                        <p style="line-height: 1; margin-top: 0; margin-bottom: 0;">{{this.[0].body}}</p>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>    
                                    <br />
                                    <p style="line-height: 1; margin-top: 0; margin-bottom: 0;"><b>Response</b></p>
                                    <div>
                                        <table style="font-family: 'Courier New', monospace;">
                                            <tbody>
                                                <tr>
                                                    <td style="line-height: 1; margin-top: 0; margin-bottom: 0;">
                                                        <p style="line-height: 1; margin-top: 0; margin-bottom: 0;">{{this.[1].httpVersion}} {{this.[1].statusCode}} {{this.[1].reasonPhrase}}</p>
                                                        {{#each this.[1].headers}}
                                                        <p style="line-height: 1; margin-top: 0; margin-bottom: 0;">{{@key}}: {{this}}</p>
                                                        {{/each}}
                                                        <br />
                                                        <p style="line-height: 1; margin-top: 0; margin-bottom: 0;">{{this.[1].body}}</p>
                                                    </td>
                                                </tr>
                                            </tbody>
                                        </table>
                                    </div>
                                    {{/each}}
                                """.trimIndent());
                                Utils.logDebug(template.apply(newReqResp))
                                return template.apply(newReqResp)
                            } catch (ex: Exception) {
                                Utils.logDebug(ex.stackTraceToString())
                                return "aaa"
                            }
                        }
                    }
                    val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    clipboard.setContents(htmlSelection, null)

                }

                return listOf(reqRespJMenuItem)
            }
        })
    }
}