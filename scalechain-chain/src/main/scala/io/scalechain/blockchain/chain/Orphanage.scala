package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.{Hash, Transaction, Block, BlockHash}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/** Keeps a list of orphans.
  *
  * I for items.
  */
class OrphanItems[I] {
  /** The list of orphans to keep.
    */
  private val items = ListBuffer[I]()

  /** Put an item on the list of orphans.
    *
    * @param item The orphan to put.
    */
  def put(item : I) : Unit = {
    items += item
  }

  /** Get the list of orphans we are keeping.
    *
    * @return The list of orphans.
    */
  def get() = items.toList
}

/** Keep a list of orphans for each (missing) dependency.
  * Whenever we receive an item such as a block or a tranasction,
  * it is kept in OrphanItems if its dependency is not received yet.
  *
  * Example of dependencies :
  * 1. Blocks - parent block
  * 2. Transactions - transactions pointed by out points.
  *
  */
class Orphanage[D,I] { // D : Dependency, I : Item
  /** Keep a list of items that has the same dependency.
    */
  private val itemsByDependency = mutable.HashMap[D, OrphanItems[I]]()

  /** Put an item as an orphan of a specific dependency.
    *
    * @param dependency The hash of the parent block.
    * @param orphanItem The orphan block.
    */
  def put(dependency : D, orphanItem : I) : Unit = {
    // Step 1 : See if we have any orphan block for the same parent block hash.
    val orphanItems = itemsByDependency.get(dependency)

    if (orphanItems.isDefined) {
      // Step 2.A : put the orphan block into the list of blocks for the parent block hash.
      orphanItems.get.put(orphanItem)
    } else {
      // Step 2.A : Create a new Blocks object, add the orphan block.
      val newOrphanItems = new OrphanItems[I]()
      newOrphanItems.put(orphanItem)
      itemsByDependency.put(dependency, newOrphanItems)
    }
  }

  /** Find orphans by the dependency.
    *
    * @param dependency The dependency such as hash of the parent block of orphan blocks.
    * @return Some orphan items if any orphan was found. None otherwise.
    */
  def findByDependency(dependency : D) : Option[OrphanItems[I]] = {
    itemsByDependency.get(dependency)
  }

  /** Remove all orphans whose dependencies match the given one..
    *
    * @param dependency The hash of the parent.
    */
  def remove(dependency : D) : Unit = {
    itemsByDependency.remove(dependency)
  }
}

/** The orphan blocks.
  *
  */
class OrphanBlocks extends Orphanage[BlockHash,Block]

/** The orphan transactions.
  *
  */
class OrphanTransactions extends Orphanage[Hash,Transaction]
