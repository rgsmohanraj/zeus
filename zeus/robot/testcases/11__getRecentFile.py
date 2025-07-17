import os
import glob


def get_most_recent_excel_file():
    # Specify your download directory path
    # download_directory = "D://karthikeyan.arumugam//OneDrive - Vivriti Capital Private Limited//Downloads"
    current_directory = os.getcwd()
    download_directory = os.path.join(
        current_directory, 'zeus', 'robot', 'PageObjects', 'testData')
    search_pattern = os.path.join(download_directory, "*.xlsx")
    files = glob.glob(search_pattern)
    # get the most recent file using file creation time
    most_recent_file = max(files, key=os.path.getctime)
    return most_recent_file


def get_most_recent_csv_file():
    # Specify your download directory path
    current_directory = os.getcwd()

    # Concatenate the directory path
    download_directory = os.path.join(
        current_directory, 'zeus', 'robot', 'PageObjects', 'testData')
    # download_directory = "D://karthikeyan.arumugam//OneDrive - Vivriti Capital Private Limited//Downloads"
    search_pattern = os.path.join(download_directory, "*.csv")
    print(search_pattern)
    files = glob.glob(search_pattern)
    # get the most recent file using file creation time
    most_recent_file = max(files, key=os.path.getctime)
    return most_recent_file


def get_most_recent_pdf_file():
    # Specify your download directory path
    current_directory = os.getcwd()

    download_directory = os.path.join(
        current_directory, 'zeus', 'robot', 'PageObjects', 'testData')
    search_pattern = os.path.join(download_directory, "*.pdf")
    print(search_pattern)
    files = glob.glob(search_pattern)
    most_recent_file = max(files, key=os.path.getctime)
    return most_recent_file
