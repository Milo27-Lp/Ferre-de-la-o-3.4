package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY description ASC")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE line = :line ORDER BY description ASC")
    fun getProductsByLine(line: String): Flow<List<ProductEntity>>

    @Query("""
        SELECT * FROM products 
        WHERE description LIKE '%' || :query || '%' 
           OR code LIKE '%' || :query || '%' 
           OR barcode LIKE '%' || :query || '%'
           OR brand LIKE '%' || :query || '%'
        ORDER BY description ASC
    """)
    fun searchProducts(query: String): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE code = :code OR barcode = :code LIMIT 1")
    suspend fun getProductByCodeOrBarcode(code: String): ProductEntity?

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Query("SELECT * FROM products WHERE stock <= minStock ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<ProductEntity>>

    @Query("SELECT DISTINCT line FROM products ORDER BY line ASC")
    fun getAllLines(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET stock = :newStock WHERE id = :id")
    suspend fun updateStock(id: Long, newStock: Double)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Long)
}
