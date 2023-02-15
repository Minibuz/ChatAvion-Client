// webdriverio as W3C capabilities

const wdio = require("webdriverio");
const assert = require("assert");

const opts = {
  path: '/wd/hub',
  port: 4723,
  capabilities: {
    platformName: "Android",
    platformVersion: "8.1",             //ANDROID VERSION OF EMULATOR
    deviceName: "advtest",              //NAME OF YOUR EMULATOR IN ANDROID STUDIO (in device manager)
    appPackage: "fr.chatavion.client",  //PACKAGE IN WHICH ACTIVITY IS IN
    appActivity: ".MainActivity",       //NAME OF TO BE TESTED ACTIVITY
    automationName: "UiAutomator2",
    disableIdLocatorAutocompletion: "true"
  }
};




async function main () {

  const driver = await wdio.remote(opts);

    //id communaute
    let selector = 'new UiSelector().resourceId("textEditCommu")';
    const fieeeld = await driver.$(`android=${selector}`)
    await fieeeld.setValue("default@chatavion.com")

    //Pseudo
    selector = 'new UiSelector().resourceId("textEditPwd")';
    const pssd = await driver.$(`android=${selector}`)
    await pssd.setValue("user");

    //bouton Rejoindre
    selector = 'new UiSelector().resourceId("connectionBtn")';
    const connect = await driver.$(`android=${selector}`)
    await connect.click();

    //bottom bar message text field
    selector = 'new UiSelector().resourceId("msgEditField")';
    const msg = await driver.$(`android=${selector}`)
    await msg.setValue("message");

    //send button
    selector = 'new UiSelector().resourceId("sendBtn")';
    const send = await driver.$(`android=${selector}`)
    await send.click();

  await driver.deleteSession();
}

main();