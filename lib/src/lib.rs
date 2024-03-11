pub mod wollet;

pub use wollet::*;

#[cfg(test)]
mod tests {
    use anyhow::Result;
    use bip39::{Mnemonic, Language};
    use lwk_common::{Singlesig, singlesig_desc};
    use lwk_signer::SwSigner;
    use std::{io, env, path::PathBuf, fs, str::FromStr, sync::Arc};
    use crate::{BreezWollet, WolletOptions, Network};

    const DEFAULT_DATA_DIR: &str = ".data";
    const PHRASE_FILE_NAME: &str = "phrase";

    fn get_mnemonic() -> Result<Mnemonic> {
        let data_dir = PathBuf::from(env::var("DATA_DIR").unwrap_or(DEFAULT_DATA_DIR.to_string()));
        fs::create_dir_all(&data_dir)?;

        let filename = data_dir.join(PHRASE_FILE_NAME);

        let mnemonic = match fs::read_to_string(filename.clone()) {
            Ok(phrase) => Mnemonic::from_str(&phrase).unwrap(),
            Err(e) => {
                if e.kind() != io::ErrorKind::NotFound {
                    panic!(
                        "Can't read from file: {}, err {e}",
                        filename.to_str().unwrap()
                    );
                }
                let mnemonic = Mnemonic::generate_in(Language::English, 24)?;
                fs::write(filename, mnemonic.to_string())?;
                mnemonic
            }
        };

        Ok(mnemonic)
    }

    async fn init_wollet() -> Result<Arc<BreezWollet>> {
        let mnemonic = get_mnemonic()?;
        let signer = SwSigner::new(&mnemonic.to_string(), false)?;
        let desc = singlesig_desc(
            &signer,
            Singlesig::Wpkh,
            lwk_common::DescriptorBlindingKey::Elip151,
            false,
        )
        .expect("Expected valid descriptor");

        BreezWollet::new(WolletOptions {
            signer,
            desc,
            electrum_url: None,
            db_root_dir: None,
            network: Network::LiquidTestnet,
        }).await
    }

    #[tokio::test]
    async fn normal_submarine_swap() -> Result<()> {
        let breez_wollet = init_wollet().await?;

        let mut invoice = String::new();
        println!("Please paste the invoice to be paid: ");
        io::stdin().read_line(&mut invoice)?;

        breez_wollet.send_payment(&invoice).await?;

        Ok(())
    }

    #[tokio::test]
    async fn reverse_submarine_swap_success() -> Result<()> {
        let breez_wollet = init_wollet().await?;

        let swap_response = breez_wollet.receive_payment(1000)?;

        println!(
            "Please pay the following invoice: {}",
            swap_response.invoice
        );

        breez_wollet.wait_balance_change().await?;

        // wollet.wait_and_claim(&swap_response.id, &swap_response.claim_details)?;

        Ok(())
    }

    #[tokio::test]
    async fn reverse_submarine_swap_recovery() -> Result<()> {
        Ok(())
    }
}