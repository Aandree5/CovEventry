<?php
class Database{
 
    // Database credentials
    const HOST = "localhost";
    const DB = "andrefms_coveventry";
    const USER = "andrefms_CEAdmin";
    const PASS = "M6HyXGP13@io";
 
    // Creates the database connections and tests for validity
    static function getConnection(){
        try {
            $conn = new PDO("mysql:host=" . self::HOST . ";dbname=" . self::DB, self::USER, self::PASS);
                
            // set the PDO error mode to exception
            $conn->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
            
            return $conn;

        } catch(PDOException $e) {
            die("Connection failed: " . $e->getMessage());
        }
    }
}
?>