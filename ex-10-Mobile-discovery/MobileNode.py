import tkinter as tk

# utils
import subprocess

meta_data = subprocess.check_output(['netsh', 'wlan', 'show', 'profiles'])
data = meta_data.decode('utf-8', errors="backslashreplace")
data = data.split('\n')
names = ""

for i in data:
    if "All User Profile" in i:
        i = i.split(":")
        i = i[1]
        i = i[1:-1]
        names += i
        names += '\n'


root = tk.Tk()

# specify size of window.
root.geometry("250x170")

# Create text widget and specify size.
T = tk.Text(root, height=30, width=60)

# Create label
l = tk.Label(root, text="Oracle")
l.config(font=("Courier", 20))

Fact = """A man can be arrested in
Italy for wearing a skirt in public."""

# Create button for next text.
b1 = tk.Button(root, text="Next", )

# Create an Exit button.
b2 = tk.Button(root, text="Exit",
               command=root.destroy)

l.pack()
T.pack()
b1.pack()
b2.pack()

# Insert The Fact.
T.insert(tk.END, names)

tk.mainloop()
