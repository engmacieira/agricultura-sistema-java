from app import create_app
import sys 
import os 

if len(sys.argv) > 1:
    USER_DATA_PATH = sys.argv[1]
else:
    USER_DATA_PATH = os.path.abspath(os.path.dirname(__file__))

app = create_app(user_data_path=USER_DATA_PATH)

if __name__ == '__main__':
    app.run(debug=False)