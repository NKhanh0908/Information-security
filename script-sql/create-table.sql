IF EXISTS DATABASE "informationsecurity" DROP DATABASE "informationsecurity"
CREATE DATABASE "informationsecurity";

CREATE TABLE "users" (
    "user_id" INT PRIMARY KEY AUTO_INCREMENT COMMENT "User ID",
    "user_name" VARCHAR(100) NOT NULL COMMENT "Name of the user",
    "user_gender" ENUM('male', 'female', 'other') NOT NULL COMMENT "Gender of the user",
    "user_date_of_birth" DATE NOT NULL COMMENT "Date of birth of the user",
    "user_address" VARCHAR(255) NOT NULL COMMENT "Address of the user",
    "user_phone" VARCHAR(20) NOT NULL COMMENT "Phone number of the user",
    "user_created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT "Account creation timestamp",
    "user_updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT "Account update timestamp"
);

CREATE TABLE "account"(
    "account_id" INT PRIMARY KEY AUTO_INCREMENT COMMENT "Account ID",
    "user_id" INT NOT NULL COMMENT "User ID",
    "account_username" VARCHAR(100) NOT NULL COMMENT "Username for the account",
    "account_password" VARCHAR(100) NOT NULL COMMENT "Password for the account",
    "account_email" VARCHAR(255) NOT NULL COMMENT "Email address associated with the account",
    "account_is_locked" BOOLEAN DEFAULT FALSE COMMENT "Is the account locked?",
    "account_locked_time" DATETIME COMMENT "Time when the account was locked",
    "account_last_login" DATETIME COMMENT "Last login time of the account",
    "account_created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT "Account creation timestamp",
    "account_updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT "Account update timestamp"
);

CREATE TABLE "login_attempt"(
    "attempt_id" INT PRIMARY KEY AUTO_INCREMENT COMMENT "Attempt ID",
    "account_id" INT NOT NULL COMMENT "Account ID",
    "attempt_ip_address" VARCHAR(45) NOT NULL COMMENT "IP address of the attempt",
    "attempt_success" BOOLEAN NOT NULL COMMENT "Was the attempt successful?",
    "attempt_user_agent" VARCHAR(255) NOT NULL COMMENT "User agent string of the attempt",
    "attempt_failure_reason" VARCHAR(255) COMMENT "Reason for failure (if any)",
    "attempt_created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT "Attempt creation timestamp"
);

CREATE TABLE "trust_device" (
    "device_id" INT PRIMARY KEY AUTO_INCREMENT COMMENT "Device ID",
    "user_id" INT NOT NULL COMMENT "User ID",
    "device_name" VARCHAR(100) NOT NULL COMMENT "Name of the device",
    "device_ip_address" VARCHAR(45) NOT NULL COMMENT "IP address of the device",
    "device_location" VARCHAR(255) NOT NULL COMMENT "Location of the device",
    "device_user_agent" VARCHAR(255) NOT NULL COMMENT "User agent string of the device",
    "device_is_active" BOOLEAN DEFAULT TRUE COMMENT "Is the device active?",
    "device_created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT "Device registration timestamp",
    "device_updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT "Device update timestamp"
);

CREATE TABLE "mfa_settings"(
    "mfa_id" INT PRIMARY KEY AUTO_INCREMENT COMMENT "MFA ID",
    "user_id" INT NOT NULL COMMENT "User ID",
    "mfa_enabled" BOOLEAN DEFAULT FALSE COMMENT "Is MFA enabled?",
    "mfa_primary_method" ENUM('TOTP', 'EMAIL', "WEBAUTHN", 'AUTHENTICATOR_APP', 'BACKUP_CODES') NOT NULL COMMENT "Primary MFA method",
    "mfa_backup_method" ENUM('TOTP', 'EMAIL', "WEBAUTHN", 'AUTHENTICATOR_APP', 'BACKUP_CODES') COMMENT "Backup MFA method",
    "mfa_totp_secret_key" VARCHAR(255) COMMENT "MFA TOTP secret key",
    "mfa_totp_enable" BOOLEAN DEFAULT FALSE COMMENT "Is TOTP enabled?",
    "mfa_backup_codes" TEXT COMMENT "Backup codes for MFA",
    "mfa_email_enabled" BOOLEAN DEFAULT FALSE COMMENT "Is email MFA enabled?",
    "mfa_webauthn_enabled" BOOLEAN DEFAULT FALSE COMMENT "Is WebAuthn MFA enabled?",
    "mfa_authenticator_app_enabled" BOOLEAN DEFAULT FALSE COMMENT "Is Authenticator App MFA enabled?",
    "mfa_required_mfa_for_sensitive_actions" BOOLEAN DEFAULT FALSE COMMENT "Is MFA required for sensitive actions?",
    "mfa_created_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT "MFA settings creation timestamp",
    "mfa_updated_at" TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT "MFA settings update timestamp"
)

-- Add foreign key constraints
ALTER TABLE account ADD CONSTRAINT fk_account_user 
    FOREIGN KEY (user_id) REFERENCES users(user_id);

ALTER TABLE login_attempt ADD CONSTRAINT fk_login_account 
    FOREIGN KEY (account_id) REFERENCES account(account_id);

ALTER TABLE trust_device ADD CONSTRAINT fk_device_user 
    FOREIGN KEY (user_id) REFERENCES users(user_id);

ALTER TABLE mfa_settings ADD CONSTRAINT fk_mfa_user 
    FOREIGN KEY (user_id) REFERENCES users(user_id);

-- Add indexes
CREATE INDEX idx_account_username ON account(account_username);
CREATE INDEX idx_account_email ON account(account_email);
CREATE INDEX idx_login_attempt_ip ON login_attempt(attempt_ip_address);
CREATE INDEX idx_login_attempt_time ON login_attempt(attempt_created_at);