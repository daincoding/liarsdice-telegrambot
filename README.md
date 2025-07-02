# LiarsDice Telegram Bot 🎲

A **work-in-progress** Telegram bot built in Java for playing Liars Dice directly in chat.

⚙️ **Tech Stack**
- Java
- TelegramBots API
- Maven

> **Status:** 🚧 WIP – actively being developed.

# 🎲 Liars Dice Telegram Bot

This bot brings the classic **Liars Dice** game into Telegram as a text-based 1-on-1 experience.  
Below are the official rules and win conditions implemented in the bot.

---

## 🏆 How to Win the Game

- Be the **last player standing** with at least **2 dice** remaining.
- Or instantly win by correctly calling the **highest possible combo** (all dice on the table showing the same value).

---

## 🎯 How to Win a Round

- Call a combination that matches the dice on the table.
- Avoid being caught lying.
- Remember: **Quantity beats face value.**
    - e.g. “3×1s” beats “2×6s.”

---

## 🎲 Ruleset

- Each player starts with **5 six-sided dice.**
- At the start of the match:
    - Each player rolls **1 die.**
    - The higher roll becomes the **Caller** and starts the round.
    - Ties are rerolled.
- All players secretly roll their 5 dice.
- The Caller starts by making a statement:
  > “I have 2 times a 5.”
- Going clockwise, players must:
    - **Raise the call** (either quantity or higher face value), or
    - **Call “YOU LIE!”**
- **Minimum call**: “1 time a 1.”

---

## 🔁 Rerolls

- Once per round, on your turn, you may reroll any number of your dice.
- After a reroll, you **must raise the call.**

---

## 🤥 Calling “YOU LIE!”

- “YOU LIE!” is allowed **at any time**, even on the first call.
- All players reveal their dice.
    - If the claim was true:
        - The challenger **loses 1 die.**
    - If the claim was false:
        - The player who made the claim **loses 2 dice.**

---

## 🚀 Instant Win

- If a player calls the **maximum possible quantity** for a number and it’s true:
    - They win the game instantly.
- The maximum quantity depends on the **total number of dice left** in play.

---

## ⚠️ Eliminations

- Any player reduced to **1 die** is immediately eliminated from the game.

---

## 📝 Example

> **2 Players:**
>
> - Both roll to decide the Caller.
>
> **Player 1’s hand:** 3×2s, 2×5s  
> **Player 2’s hand:** 3×3s, 2×6s
>
> - Player 1: “I have 2 times a 5.”
> - Player 2: “I have 2 times a 6.”
> - Player 1: “I have 3 times a 2.”
> - Player 2: “I have 3 times a 3.”
> - Player 1 rerolls 2 dice, resulting in:
    >     - 3×2s, 1×3, 1×6
> - Player 1: “I have 4 times a 3.”
> - Player 2: “YOU LIE!”
>
> Both reveal dice:
> - Total 3s on table = 4 → Player 1 was **telling the truth**.
> - Player 2 loses 1 die.