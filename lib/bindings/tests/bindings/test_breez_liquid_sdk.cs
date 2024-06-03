
using breez_liquid_sdk.breez_liquid_sdk;

try
{
    var mnemonic = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";

    var config = BreezLiquidSdkMethods.DefaultConfig(Network.Testnet);

    var connectReq = new ConnectRequest(config, mnemonic);
    BindingLiquidSdk sdk = BreezLiquidSdkMethods.Connect(connectReq);

    var getInfoReq = new GetInfoRequest(false);
    GetInfoResponse? info = sdk.GetInfo(getInfoReq);

    Console.WriteLine(info!.pubkey);
}
catch (Exception e)
{
    Console.WriteLine(e.Message);
    throw;
}
