import json
import logging
import os
import time

logger = logging.getLogger()
logger.setLevel(logging.INFO)

def handler(event, context):
    """
    Lambda function for fleet provisioning pre-provisioning hook.
    This function validates the device before allowing provisioning.
    """
    # Log the event and context for debugging
    logger.info(f"Received event: {json.dumps(event)}")
    logger.info(f"Context: {context}")
    
    # Add a timestamp for debugging
    logger.info(f"Function invoked at: {time.time()}")
    
    # Extract parameters from the event
    try:
        parameters = event.get('parameters', {})
        serial_number = parameters.get('SerialNumber')
        certificate_id = parameters.get('AWS::IoT::Certificate::Id')
        
        logger.info(f"Processing device with serial number: {serial_number} and certificate ID: {certificate_id}")
        
        # Here you would typically validate the device against a database or other source
        # For this example, we'll just check if the serial number starts with 'AAA'
        if serial_number and serial_number.startswith('AAA'):
            logger.info(f"Device with serial number {serial_number} is authorized")
            return {
                'allowProvisioning': True
            }
        else:
            logger.warning(f"Device with serial number {serial_number} is NOT authorized")
            return {
                'allowProvisioning': False
            }
            
    except Exception as e:
        logger.error(f"Error processing request: {str(e)}")
        return {
            'allowProvisioning': False
        }
