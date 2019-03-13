<?php

    include_once 'config/database.php';

    $id = $_POST['id'];
    $facebook_id = $_POST['facebook_id'];
    $twitter_id = $_POST['twitter_id'];

    if(!$id && !$facebook_id && !$twitter_id)
    {
        header('Location: error.php'); // Error for unauthorized access
        die;
    }
    
    $conn = Database::getConnection();

    try {

        if ($id) {
            $stmt = $conn->prepare("DELETE FROM Users 
                                    WHERE id = :id" );
            
            $id = htmlspecialchars(strip_tags($id));
            
            $stmt->bindParam(":id", $id);

            if ($stmt->execute()) {
                $data["status"] = "successful";
           
                echo json_encode($data);
            }

        } else if ($facebook_id) {
            $stmt = $conn->prepare("UPDATE Users 
                                    SET facebook_id = NULL
                                    WHERE facebook_id = :facebook_id");

            // Bind parameters
            $stmt->bindParam(":facebook_id", $facebook_id);

            if ($stmt->execute())
            {
                $data["status"] = "successful";

                echo json_encode($data);
            }

        } else if ($twitter_id) {
            $stmt = $conn->prepare("UPDATE Users 
                                    SET twitter_id = NULL, verified = 0
                                    WHERE twitter_id = :twitter_id");

            // Bind parameters
            $stmt->bindParam(":twitter_id", $twitter_id);

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