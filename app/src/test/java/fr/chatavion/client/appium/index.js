// webdriverio as W3C capabilities

const wdio = require("webdriverio");
const assert = require("assert");

const opts = {
  path: '/wd/hub',
  port: 4723,
  capabilities: {
    platformName: "Android",
    platformVersion: "8.1",//ANDROID VERSION OF EMULATOR
    deviceName: "advtest", //EMULTAOR NAME IN DEVICE MANAGER
    appPackage: "fr.chatavion.client", //PACKAGE WHERE TO BE TESTED ACTIVITY IS
    appActivity: ".MainActivity", //TO BE TESTED ACTIVITY NAME
    automationName: "UiAutomator2" //DONT TOUCH
  }
};

/*
REQUIREMENTS

EMULATOR RUNNING WITH APP INSTALLED
APPIUM SERVER RUNNING ON 0.0.0.0 OR 127.0.0.1 AND PORT 4723 SAME AS PORT OPTION IN opts
*/

async function main () {
  const driver = await wdio.remote(opts);

    let selector = 'new UiSelector().resourceId("textEditCommu")'
    const commu = await driver.$(`android=${selector}`)
    await commu.setValue("default@chatavion.com");

    selector = 'new UiSelector().resourceId("textEditPwd")'
   const psd = await driver.$(`android=${selector}`)
   await psd.setValue("pseudal");

   selector = 'new UiSelector().resourceId("connectionBtn")'
   const joinBtn = await driver.$(`android=${selector}`)
   await joinBtn.click();

   selector = 'new UiSelector().resourceId("msgEditField")'
   const editField = await driver.$(`android=${selector}`)
   await editField.setValue("mesaje");

   selector = 'new UiSelector().resourceId("sendBtn")'
      const sendBtn = await driver.$(`android=${selector}`)
      await sendBtn.click();
/*
    const field = await client.$("android.widget.EditText");
    await field.setValue("Hello World!");
    const value = await field.getText();
    assert.strictEqual(value, "Hello World!");*/
  await driver.deleteSession();
}

main();