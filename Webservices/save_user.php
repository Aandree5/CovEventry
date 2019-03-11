<?php

    include_once 'config/database.php';

    $id = $_POST['id'];
    $name = $_POST['name'];
    $username = $_POST['username'];
    $email = $_POST['email'];
    $profile_picture = $_POST['profile_picture'];
    $facebook_id = $_POST['facebook_id'];
    $twitter_id = $_POST['twitter_id'];
    $verified = $_POST['verified'];

    if(!$name || !$email || $verified)
    {
        header('Location: error.php'); // Error for unauthorized access
        die;
    }
    
    $conn = Database::getConnection();

    try { 
        // First time user connects accont on a device, check if account is already connected
        // for Facebook and Twitter, both seperate
        // If already registered. returns the id
        if (!$id && $facebook_id) {
            $stmt = $conn->prepare("SELECT id FROM Users WHERE facebook_id = :facebook_id");
            
            $facebook_id = htmlspecialchars(strip_tags($facebook_id));
            
            $stmt->bindParam(":facebook_id", $facebook_id);

            if ($stmt->execute()) {
                $row = $stmt->fetch(PDO::FETCH_ASSOC);
                
                if ($row)
                    $id = $row["id"];
            }

        } 
        
        if (!$id && $twitter_id) {
            $stmt = $conn->prepare("SELECT id FROM Users WHERE twitter_id = :twitter_id");
            
            $twitter_id = htmlspecialchars(strip_tags($twitter_id));
            
            $stmt->bindParam(":twitter_id", $twitter_id);

            if ($stmt->execute()) {
                $row = $stmt->fetch(PDO::FETCH_ASSOC);
                
                if ($row)
                    $id = $row["id"];

                    
            }

        }
        

        // Sanitize user input
        $id = htmlspecialchars(strip_tags($id));
        $name = htmlspecialchars(strip_tags($name));
        $username = htmlspecialchars(strip_tags($username));
        $email = htmlspecialchars(strip_tags($email));
        $profile_picture = htmlspecialchars(strip_tags($profile_picture));
        $facebook_id = htmlspecialchars(strip_tags($facebook_id));
        $twitter_id = htmlspecialchars(strip_tags($twitter_id));
        $verified = htmlspecialchars(strip_tags($verified));

        // Convert from string to int, to be used as a bit type on the database
        $verified = ($verified == "1" ? 1 : 0);

        // If id exists, update details otherwise create user
        if ($id)
        {
            $stmt = $conn->prepare("UPDATE Users SET name = :name, username = :username, email = :email,
                                                    profile_picture = :profile_picture, facebook_id = :facebook_id,
                                                    twitter_id = :twitter_id, verified = :verified
                                                    WHERE id = :id");

            $stmt->bindParam(":id", $id, PDO::PARAM_INT);
            $stmt->bindParam(":name", $name);
            $stmt->bindParam(":username", $username);
            $stmt->bindParam(":email", $email);
            $stmt->bindParam(":profile_picture", $profile_picture);
            $stmt->bindParam(":facebook_id", $facebook_id);
            $stmt->bindParam(":twitter_id", $twitter_id);
            $stmt->bindParam(":verified", $verified, PDO::PARAM_INT);
            
            if ($stmt->execute())
            {
                $data["status"] = "successful";
           
                echo json_encode($data);
            }

        } else {
            $stmt = $conn->prepare("INSERT INTO Users(name, username, email, profile_picture, facebook_id, twitter_id, verified) 
                                            VALUES (:name, :username, :email, :profile_picture, :facebook_id, :twitter_id, :verified)");

            $stmt->bindParam(":name", $name);
            $stmt->bindParam(":username", $username);
            $stmt->bindParam(":email", $email);
            $stmt->bindParam(":profile_picture", $profile_picture);
            $stmt->bindParam(":facebook_id", $facebook_id);
            $stmt->bindParam(":twitter_id", $twitter_id);
            $stmt->bindParam(":verified", $verified, PDO::PARAM_INT);
            
            if ($stmt->execute())
            {
                $data["status"] = "successful";
           
                echo json_encode($data);
            }

        }

    } catch(PDOException $e) {
        echo "Error: " . $e->getMessage();
    }

    $conn = null;
?>