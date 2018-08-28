package bitcoin.wallet.kit.managers

import bitcoin.wallet.kit.blocks.BlockSyncer
import bitcoin.wallet.kit.headers.HeaderSyncer
import bitcoin.wallet.kit.models.Header
import bitcoin.wallet.kit.models.InventoryItem
import bitcoin.wallet.kit.models.MerkleBlock
import bitcoin.wallet.kit.models.Transaction
import bitcoin.wallet.kit.network.PeerGroup
import bitcoin.walllet.kit.io.BitcoinInput
import org.slf4j.LoggerFactory

class Syncer(private val peerGroup: PeerGroup, private val headerSyncer: HeaderSyncer, private val blockSyncer: BlockSyncer) : PeerGroup.Listener {

    enum class SyncStatus {
        Syncing, Synced, Error
    }

    private val log = LoggerFactory.getLogger(Syncer::class.java)

    override fun onReady() {
        try {
            headerSyncer.sync()
        } catch (e: Exception) {
            log.error("Header Syncer Error", e)
        }

        blockSyncer.run()
    }

    override fun onReceiveHeaders(headers: Array<Header>) {
//        TODO("not implemented")
    }

    override fun onReceiveMerkleBlock(merkleBlock: MerkleBlock) {
//        TODO("not implemented")
    }

    override fun onReceiveTransaction(transaction: Transaction) {
//        TODO("not implemented")
    }

    override fun shouldRequest(inventory: InventoryItem): Boolean {
//        TODO("not implemented")
        return true
    }

    override fun getTransaction(hash: String): Transaction {
//        TODO("not implemented")
        return Transaction(BitcoinInput(byteArrayOf()))
    }

}
