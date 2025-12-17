package ru.spb.ipo.generator.image_generator.service;

import ru.spb.ipo.generator.image_generator.cdsl.interpreter.ProblemContext;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageGeneratorService {

    public BufferedImage generateImage(ProblemContext context) {
        if (context == null || context.getProblemType() == null) {
            return generateErrorImage("No problem context");
        }

        try {
            // Делегируем работу специализированным генераторам
            switch (context.getProblemType()) {
                case CARDS:
                    System.out.println("ImageGeneratorService: Генерация изображения для задачи с картами");
                    CardsImageGenerator cardsGen = new CardsImageGenerator();
                    return cardsGen.generateImage(context);
                case WORDS:
                    WordsImageGenerator wordsGen = new WordsImageGenerator();
                    return wordsGen.generateImage(context);
                case CHESS:
                    ChessImageGenerator chessGen = new ChessImageGenerator();
                    return chessGen.generateImage(context);
                case NUMBERS:
                    NumbersImageGenerator numbersGen = new NumbersImageGenerator();
                    return numbersGen.generateImage(context);
                case EQUATIONS:
                    EquationsImageGenerator equationsGen = new EquationsImageGenerator();
                    return equationsGen.generateImage(context);
                case DIVISIBILITY:
                    DivisibilityImageGenerator divisibilityGen = new DivisibilityImageGenerator();
                    return divisibilityGen.generateImage(context);
                case REMAINDERS:
                    RemaindersImageGenerator remaindersGen = new RemaindersImageGenerator();
                    return remaindersGen.generateImage(context);
                case BALLS_AND_URNS:
                    BallsImageGenerator ballsGen = new BallsImageGenerator();
                    return ballsGen.generateImage(context);
                default:
                    return generateErrorImage("Unknown problem type");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return generateErrorImage("Generation error: " + e.getMessage());
        }
    }

    private BufferedImage generateErrorImage(String message) {
        BufferedImage image = new BufferedImage(400, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, 400, 100);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString(message, 50, 50);

        g2d.dispose();
        return image;
    }
}