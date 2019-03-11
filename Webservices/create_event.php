<?php

    include_once 'config/database.php';

    $host_id = $_POST['host_id'];
    $title = $_POST['title'];
    $description = $_POST['description'];
    $image = $_POST['image'];
    $venue = $_POST['venue'];
    $post_code = $_POST['post_code'];
    $date = $_POST['date'];

    if(!$host_id || !$title || !$description || !$venue || !$post_code || !$date)
    {
        header('Location: error.php'); // Error for unauthorized access
        die;
    }
    
    $conn = Database::getConnection();

    try {    
        // Prepare SQL and bind parameters
        $stmt = $conn->prepare("INSERT INTO Events(host_id, title, description, image, venue, post_code, date) 
        VALUES(:host_id, :title, :description, :image, :venue, :post_code, :date)");

        // Sanitize user input
        $host_id = htmlspecialchars(strip_tags($host_id));
        $title = htmlspecialchars(strip_tags($title));
        $description = htmlspecialchars(strip_tags($description));
        $venue = htmlspecialchars(strip_tags($venue));
        $post_code = htmlspecialchars(strip_tags($post_code));
        $date = htmlspecialchars(strip_tags($date));

        // Bind values
        $stmt->bindParam(":host_id", $host_id);
        $stmt->bindParam(":title", $title);
        $stmt->bindParam(":description", $description);
        $stmt->bindParam(":image", $image);
        $stmt->bindParam(":date", $date);
        $stmt->bindParam(":venue", $venue);
        $stmt->bindParam(":post_code", $post_code);
        $stmt->bindParam(":date", $date);



        if ($stmt->execute())
        {
            $data["status"] = "successful";
       
            echo json_encode($data);
        }

    } catch(PDOException $e) {
        echo "Error: " . $e->getMessage();
    }

    $conn = null;
?>