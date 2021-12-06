<?php

    class dbconnect{
        private $con;

        function __construct(){

        }
        function connect(){
            include_once dirname(__FILE__).'/constants.php';
            $this->con = new mysqli(dbhost,dbuser,dbpassword,dbname);

            if (mysqli_connect_errno()){
                echo "Failed to connect with database".mysqli_connect_error();
            }

            return $this -> con;
        }
    }