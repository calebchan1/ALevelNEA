<?php

    require_once '../includes/dboperations.php';
    $response = array();



    if($_SERVER['REQUEST_METHOD']=='POST'){
        //handling if all the required fields are set
        if (
            isset($_POST['username']) and
            isset($_POST['password']) and
            isset($_POST['firstname']) and
            isset($_POST['surname']) and 
            isset($_POST['dateOfBirth'])
        ){
            $db = new dboperations();
            
            if ($db ->createuser(
                $_POST['username'],
                $_POST['password'],
                $_POST['firstname'],
                $_POST['surname'],
                $_POST['dateOfBirth'])

            ){
                $response['error']=false;
                $response['message']="User registered successfully";
            }
            else{
                $response['error']=true;
                $response['message']="Error";
            }

        }
            else{
            $response['error']=true;
            $response['message']="Required fields are missing";
        }


    }else{
        $response['error']=true;
        $response['message']="Invalid Request";
    }
    echo json_encode($response);