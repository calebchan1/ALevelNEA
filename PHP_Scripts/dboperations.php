<?php

    class dboperations{
        private $con;

        function __construct()
        {
            require_once dirname(__FILE__).'/dbconnect.php';
            //creating instance of dbconnect
            $db = new dbconnect();
            $this ->con = $db->connect();
        }

        //CRUD
        
        //CREATE
        function createuser($username,$password,$firstname,$lastname,$dateofbirth){
            $password = md5($password);
            $temp = $this->con->prepare(
                //SQL statement to add a new user
                "INSERT INTO `User`(`username`, `password`, `firstname`, `surname`, `dateOfBirth`) 
                VALUES (?,?,?,?,?);"
            );
            $temp->bind_param("sssss",$username,$password,$firstname,$lastname,$dateofbirth);
            
            //if was executed succesfully
            if($temp->execute()){
                return true;
            }else{
                return false;
            }
        }
    }