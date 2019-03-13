<?php

    include_once 'config/database.php';

    $id = $_POST['id'];
    $facebook_id = $_POST['facebook_id'];
    $twitter_id = $_POST['twitter_id'];

    if(!$facebook_id && !$twitter_id)
    {
        header('Location: error.php'); // Error for unauthorized access
        die;
    }
    
    $conn = Database::getConnection();

    try { 
        $stmt = $conn->prepare("SELECT * FROM Users WHERE id = :id OR facebook_id = :facebook_id OR twitter_id = :twitter_id");
        
        $id = htmlspecialchars(strip_tags($id));
        $facebook_id = htmlspecialchars(strip_tags($facebook_id));
        $twitter_id = htmlspecialchars(strip_tags($twitter_id));
        
        $stmt->bindParam(":id", $id);
        $stmt->bindParam(":facebook_id", $facebook_id);
        $stmt->bindParam(":twitter_id", $twitter_id);



        if ($stmt->execute()) {
            $row = $stmt->fetch(PDO::FETCH_ASSOC);
            
            if ($row)
                echo json_encode($row);
        }

    } catch(PDOException $e) {
        echo "Error: " . $e->getMessage();
    }

    $conn = null;
?>