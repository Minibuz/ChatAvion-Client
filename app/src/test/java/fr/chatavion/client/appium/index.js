// webdriverio as W3C capabilities

const wdio = require("webdriverio");
const assert = require("assert");

const opts = {
  path: '/wd/hub',
  port: 4723,
  capabilities: {
    platformName: "Android",
    platformVersion: "8.1",//ANDROID VERSION OF EMULATOR
    deviceName: "testadv", //EMULTAOR NAME IN DEVICE MANAGER
    appPackage: "fr.chatavion.client", //PACKAGE WHERE TO BE TESTED ACTIVITY IS
    appActivity: ".MainActivity", //TO BE TESTED ACTIVITY NAME
    automationName: "UiAutomator2" //DONT TOUCH
  }
};

function sendMessages(driver, nb){
	counter = 0;
	for(let i = 0; i < nb; i++){
		await setTimeout(function () {
	        selector = 'new UiSelector().resourceId("msgEditField")'
	        let editField =  driver.$(`android=${selector}`)
	         editField.setValue("appiumMessage"+i);

        await setTimeout(function() {
            selector = 'new UiSelector().resourceId("sendBtn")'
            let sendBtn =  driver.$(`android=${selector}`)
            sendBtn.click();
            }, 500)

	    }, 1000);
    }
}

/*
REQUIREMENTS

EMULATOR RUNNING WITH APP INSTALLED
APPIUM SERVER RUNNING ON 0.0.0.0 OR 127.0.0.1 AND PORT 4723 SAME AS PORT OPTION IN opts
*/

async function main () {
  const driver = await wdio.remote(opts);

    let selector = 'new UiSelector().resourceId("textEditCommu")'
    const commu = await driver.$(`android=${selector}`)
    await commu.setValue("test@chatavion.com");

    selector = 'new UiSelector().resourceId("textEditPwd")'
   const psd = await driver.$(`android=${selector}`)
   await psd.setValue("appiumteste");

   selector = 'new UiSelector().resourceId("connectionBtn")'
   const joinBtn = await driver.$(`android=${selector}`)
   await joinBtn.click();
/*
   selector = 'new UiSelector().resourceId("msgEditField")'
   const editField = await driver.$(`android=${selector}`)
   await editField.setValue("mesaje");

   selector = 'new UiSelector().resourceId("sendBtn")'
      const sendBtn = await driver.$(`android=${selector}`)
      await sendBtn.click();
*/
/*
	await setTimeout(function () {
		selector = 'new UiSelector().resourceId("msgEditField")'
		const editField =  driver.$(`android=${selector}`)
	     editField.setValue("appiumMessage")

		setTimeout(function() {
		    selector = 'new UiSelector().resourceId("sendBtn")'
		    const sendBtn =  driver.$(`android=${selector}`)
		    sendBtn.click();
		    }, 1000)
	}, 10000)*/
//TEST ENVOI DE MESSAGE
//////////////////////////////////////////////////////////////////////////////////////////////////
	sendMessages(driver, 5)


	selector = 'new UiSelector().resourceId("paramSwitch")'
    const paramSwitch = await driver.$(`android=${selector}`)
    await paramSwitch.click();

    selector = 'new UiSelector().resourceId("PseudoParam")'
    const PseudoParam = await driver.$(`android=${selector}`)
    await PseudoParam.click();

	selector = 'new UiSelector().resourceId("pseudoChangeField")'
    const pseudoChangeField = await driver.$(`android=${selector}`)
    await pseudoChangeField.click();

	selector = 'new UiSelector().resourceId("pseudoChangeButton")'
    const pseudoChangeButton = await driver.$(`android=${selector}`)
    await pseudoChangeButton.click();

	driver.back();







//		setTimeout(function() {
//		selector = 'new UiSelector().resourceId("pseudoTag")'
//        const latestPseudo =  driver.$$(`android=${selector}`)
//
//        //.$(`android=${'new UiSelector().text("appiumMessage")'}`)
//		//setTimeout(function(){
//
//
//		//}, 1000)
//
//
//        selector = 'new UiSelector().resourceId("messageTag")'
//        }, 3000)


    setTimeout(function () {
        driver.deleteSession();
      }, 15000)

/*
    const field = await client.$("android.widget.EditText");
    await field.setValue("Hello World!");
    const value = await field.getText();
    assert.strictEqual(value, "Hello World!");*/


  //await driver.deleteSession();
}

main();