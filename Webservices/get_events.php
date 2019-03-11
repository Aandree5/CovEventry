<?php

    include_once 'config/database.php';

    $date = $_POST['date'];

    if(!$date)
    {
        header('Location: error.php'); // Error for unauthorized access
        die;
    }
    
    $conn = Database::getConnection();

    try {    
        // Prepare SQL and bind parameters
        $stmt = $conn->prepare("SELECT e.*, u.name as host_name
                                FROM Events as e, Users as u 
                                WHERE e.host_id = u.id AND DATE(date) = :date");

        // Sanitize user input
        $date = htmlspecialchars(strip_tags($date));

        // Bind values
        $stmt->bindParam(":date", $date);
        
        if ($stmt->execute())
        {
            // Store information in an array of maps, to be converted to JSON after
            $i = 0;
            while ($row = $stmt->fetch(PDO::FETCH_ASSOC)) {
                
                $data[$i]["id"] = $row["id"];
                $data[$i]["host_id"] = $row["host_id"];
                $data[$i]["host_name"] = $row["host_name"];
                $data[$i]["title"] = $row["title"];
                $data[$i]["description"] = $row["description"];
                $data[$i]["image"] = $row["image"];
                $data[$i]["venue"] = $row["venue"];
                $data[$i]["post_code"] = $row["post_code"];
                $data[$i]["date"] = $row["date"];
                $data[$i]["created"] = $row["created"];

                $i += 1;
            }   

            echo json_encode($data);
        }

    } catch(PDOException $e) {
        echo "Error: " . $e->getMessage();
    }

    $conn = null;
?>