const { Gateway, Wallets, } = require('fabric-network');
const fs = require('fs');
const path = require("path")
const log4js = require('log4js');
const logger = log4js.getLogger('BasicNetwork');
const util = require('util')
const dbConn = require('./db');


const helper = require('./helper')
const query = async (channelName, chaincodeName, args, fcn, username, org_name, data) => {

    try {

        // load the network configuration
        // const ccpPath = path.resolve(__dirname, '..', 'config', 'connection-org1.json');
        // const ccpJSON = fs.readFileSync(ccpPath, 'utf8')
        const ccp = await helper.getCCP(org_name) //JSON.parse(ccpJSON);

        // Create a new file system based wallet for managing identities.
        const walletPath = await helper.getWalletPath(org_name) //.join(process.cwd(), 'wallet');
        const wallet = await Wallets.newFileSystemWallet(walletPath);
        console.log(`Wallet path: ${walletPath}`);

        // Check to see if we've already enrolled the user.
        let identity = await wallet.get(username);
        if (!identity) {
            console.log(`An identity for the user ${username} does not exist in the wallet, so registering user`);
            await helper.getRegisteredUser(username, org_name, true)
            identity = await wallet.get(username);
            console.log('Run the registerUser.js application before retrying');
            return;
        }

        // Create a new gateway for connecting to our peer node.
        const gateway = new Gateway();
        await gateway.connect(ccp, {
            wallet, identity: username, discovery: { enabled: true, asLocalhost: true }
        });

        // Get the network (channel) our contract is deployed to.
        const network = await gateway.getNetwork(channelName);

        // Get the contract from the network.
        const contract = network.getContract(chaincodeName);
        let result;

        if (fcn == "GetUser") {
            result = await contract.evaluateTransaction(fcn, args[0]);

        } else if (fcn == "Read" || fcn == "Write") {
            result = await contract.evaluateTransaction("Set", args[0]);
            if (result.permission == '11'){
                if (fcn == "Write"){
                    dbConn.execute("INSERT INTO record (name, data) VALUES(?, ?, ?, ?)", [username, data])
                    message = `Write data successfuly`
                }
                if (fcn == "Read"){
                    r_data = dbConn.execute("SELECT data FROM users WHERE name = ?", [username])
                    message = `Read data successfuly :`
                }
            }
            else if (result.permission == '10'){
                if (fcn == "Write"){
                    dbConn.execute("INSERT INTO record (name, data) VALUES(?, ?, ?, ?)", [username, data])
                    message = `Write data successfuly`
                }
                else{
                    message = `Permission Denied`
                }
            }
            else{
                message = `Permission Denied`
            }

        }
        console.log(result)
        console.log(`Transaction has been evaluated, result is: ${result.toString()}`);

        result = JSON.parse(result.toString());
        return result
    } catch (error) {
        console.error(`Failed to evaluate transaction: ${error}`);
        return error.message

    }
}

exports.query = query