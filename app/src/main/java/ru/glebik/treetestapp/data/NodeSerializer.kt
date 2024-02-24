package ru.glebik.treetestapp.data

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

class NodeSerializer {

    fun serialize(rootNode: Node, file: File) {
        ObjectOutputStream(FileOutputStream(file)).use { outputStream ->
            outputStream.writeObject(rootNode)
        }
    }

    fun deserialize(file: File): Node? {
        try {
            ObjectInputStream(FileInputStream(file)).use { inputStream ->
                return inputStream.readObject() as Node
            }
        } catch (ex: Exception) {
            return null
        }
    }
}