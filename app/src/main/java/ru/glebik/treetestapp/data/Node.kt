package ru.glebik.treetestapp.data

import java.io.Serializable
import java.security.MessageDigest
import java.util.UUID

data class Node(
    val parent: Node? = null,
    val childs: MutableList<Node> = arrayListOf(),
    val id: UUID = UUID.randomUUID(),
) : Serializable {

    fun getName(): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(this.hashCode().toString().toByteArray())


        val hexString = hashBytes.joinToString("") { "%02x".format(it) }
        val lastIndex = hexString.length - 20 * 2
        return hexString.substring(lastIndex)
    }

    fun addChild() {
        val child = Node(
            parent = this, childs = arrayListOf()
        )
        childs.add(child)
    }

    fun deleteChild(child: Node) {
        childs.remove(child)
    }

    fun getRoot(): Node {
        var node = this
        while (node.parent != null) {
            node = node.parent!!
        }
        return node
    }

    override fun hashCode(): Int {
        return this.id.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Node) return false

        return id == other.id && childs == other.childs
    }

    override fun toString(): String {
        return "{name: ${getName()}, parent: $parent, childsSize: ${childs.size}}"
    }
}
